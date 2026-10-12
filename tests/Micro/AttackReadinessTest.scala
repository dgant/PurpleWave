package bwapi {
  // Allocate without a live BWAPI connection; record only commands used by Combat.charge.
  class AttackReadinessTestUnit extends Unit(null, 0, null) {
    var commands: Vector[String] = Vector.empty
    override def move(to: Position): Boolean = { commands :+= "move"; true }
    override def attack(target: Unit): Boolean = { commands :+= "attack"; true }
  }
}

package Micro {
  import Lifecycle.With
  import Lifecycle.Configure.Configuration
  import Mathematics.Points.Pixel
  import Micro.Actions.Combat.Decisionmaking.Combat
  import Micro.Agency.AttackDelay
  import Performance.{Cache, KeyedCache, Latency, ReactionTimes}
  import ProxyBwapi.Players.PlayerInfo
  import ProxyBwapi.ProxyBWAPI
  import ProxyBwapi.Races.{Protoss, Terran, Zerg}
  import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
  import ProxyBwapi.UnitInfo.{BWAPICachedUnitProxy, CombatUnit, FriendlyUnitInfo, UnitInfo}
  import ProxyBwapi.UnitTracking.{UnitTracker, Visibility}

  class ReadinessTarget extends BWAPICachedUnitProxy(null, 0) {
    def remainingCompletionFrames: Int = 0
    override val loadedUnitCount: Int = 0
    // Damage prediction is independent of choosing and issuing an attack order.
    override def addFutureAttack(source: CombatUnit): Unit = {}
  }

  object AttackReadinessTest extends App {
    val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
    unsafeField.setAccessible(true)
    val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
    def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
    def set(obj: AnyRef, name: String, value: Any): Unit = {
      val fields = Iterator.iterate[Class[_]](obj.getClass)(_.getSuperclass).takeWhile(_ != null)
        .flatMap(_.getDeclaredFields).filter(f => f.getName == name || f.getName.endsWith("$$" + name)).toVector
      assert(fields.nonEmpty, name)
      fields.foreach(f => { f.setAccessible(true); f.set(obj, value.asInstanceOf[AnyRef]) })
    }
    var passed = 0
    def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }

    With.frame = 100
    With.proxy = allocate(classOf[ProxyBWAPI])
    set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
    With.units = new UnitTracker
    With.configuration = new Configuration
    With.mapPixelWidth = 4096
    With.mapPixelHeight = 4096
    With.latency = allocate(classOf[Latency])
    set(With.latency, "_remainingFrames", 2)
    set(With.latency, "_turnSize", 1)
    With.reaction = new ReactionTimes {
      override def agencyMin: Int = 1
      override def agencyAverage: Int = 1
      override def sluggishness: Int = 0
    }
    val enemy = new PlayerInfo(null) {
      override lazy val isNeutral = false
      override lazy val isUs = false
      override lazy val isAlly = false
      override lazy val isEnemy = true
    }
    def target(x: Int = 300, y: Int = 200): UnitInfo = {
      val t = allocate(classOf[ReadinessTarget])
      set(t, "bwapiUnit", allocate(classOf[bwapi.AttackReadinessTestUnit]))
      set(t, "_unitClass", Zerg.Overlord)
      set(t, "_player", enemy)
      set(t, "_pixel", Pixel(x, y))
      set(t, "_flying", true)
      set(t, "_velocityX", 1.0)
      set(t, "_visibility", Visibility.Visible)
      set(t, "canMoveCache", new Cache(() => true))
      set(t, "canBeAttackedCache", new Cache(() => true))
      set(t, "_effectivelyCloaked", new Cache(() => false))
      set(t, "_presumptiveStepCache", new KeyedCache(() => t.pixel.add(100, 0), () => With.frame))
      t
    }
    def unit(kind: UnitClass = Protoss.Corsair, cooldown: Int = 8, heading: Double = 0): FriendlyUnitInfo = {
      val u = allocate(classOf[FriendlyUnitInfo])
      val api = allocate(classOf[bwapi.AttackReadinessTestUnit])
      api.commands = Vector.empty
      set(u, "bwapiUnit", api)
      set(u, "_unitClass", kind)
      set(u, "_pixel", Pixel(200, 200))
      set(u, "_angleRadians", heading)
      set(u, "_flying", kind.isFlyer)
      set(u, "friendly", Some(u))
      set(u, "_squad", None)
      set(u, "_transport", None)
      set(u, "_orderTarget", None)
      set(u, "_cooldownRaw", cooldown)
      set(u, "_cooldownLeft", new Cache(() => u.cooldownRaw, refreshPeriod = 0))
      set(u, "_loadedUnitsCache", new Cache(() => Vector.empty[FriendlyUnitInfo]))
      set(u, "canMoveCache", new Cache(() => true))
      set(u, "canAttackCache", new Cache(() => kind.rawCanAttack))
      set(u, "pixelRangeAirCache", new Cache(() => kind.pixelRangeAir))
      set(u, "pixelRangeGroundCache", new Cache(() => kind.pixelRangeGround))
      set(u, "topSpeedPossibleCache", new Cache(() => kind.topSpeed))
      set(u, "_confidence", new Cache(() => 1.0))
      u.lastFrameStartingAttack = With.frame - 24
      u
    }
    val ahead = target()
    val behind = target(100)
    check("targetless readiness never reserves a turn") {
      for (kind <- Vector(Protoss.Corsair, Protoss.Reaver); heading <- Vector(0.0, Math.PI)) {
        assert(!unit(kind, 4, heading).readyForAttackOrder)
        assert(unit(kind, 3, heading).readyForAttackOrder)
      }
    }
    check("an aligned Corsair keeps its cooldown movement window") {
      val u = unit()
      assert(u.framesToBeReadyForAttackOrder(ahead) == 5)
      assert(!u.readyForAttackOrder(ahead))
      set(u, "_cooldownRaw", 3)
      assert(u.readyForAttackOrder(ahead))
    }
    check("a turn towards the supplied target allows an earlier attack order") {
      val u = unit()
      assert(!u.readyForAttackOrder)
      assert(u.framesToBeReadyForAttackOrder(behind) == 0)
      assert(u.readyForAttackOrder(behind))
      set(u, "_cooldownRaw", 9)
      assert(!u.readyForAttackOrder(behind))
    }
    check("candidate readiness uses the argument instead of the currently selected target") {
      val u = unit()
      u.agent.toAttack = Some(behind)
      assert(!u.readyForAttackOrder(ahead))
      u.agent.toAttack = Some(ahead)
      assert(u.readyForAttackOrder(behind))
    }
    check("partial turns and angle wraparound use the shortest turn") {
      assert(unit().framesToBeReadyForAttackOrder(target(200, 300)) == 2)
      assert(unit(heading = 2 * Math.PI - 0.01).framesToBeReadyForAttackOrder(ahead) == 4)
      assert(unit(heading = 0.01).framesToBeReadyForAttackOrder(ahead) == 4)
    }
    check("independent turrets do not reserve a body turn") {
      for (kind <- Vector(Terran.Goliath, Terran.SiegeTankUnsieged, Protoss.Dragoon)) {
        val u = unit(kind, 4)
        assert(u.framesToBeReadyForAttackOrder(behind) == 1)
        assert(!u.readyForAttackOrder(behind))
        set(u, "_cooldownRaw", 3)
        assert(u.readyForAttackOrder(behind))
      }
    }
    check("both overloads retain the can-attack guard") {
      val u = unit(Protoss.Shuttle, 0)
      assert(!u.readyForAttackOrder && !u.readyForAttackOrder(ahead))
    }
    check("latency is still accounted for by both overloads") {
      val u = unit(cooldown = 4)
      set(With.latency, "_remainingFrames", 3)
      assert(u.readyForAttackOrder && u.readyForAttackOrder(ahead))
      set(With.latency, "_remainingFrames", 2)
      assert(!u.readyForAttackOrder && !u.readyForAttackOrder(ahead))
    }

    val charge = classOf[Combat].getDeclaredMethod("charge")
    charge.setAccessible(true)
    def prepare(u: FriendlyUnitInfo, t: UnitInfo): Unit = {
      u.agent.toAttack = Some(t)
      u.agent.perch.set(u.pixel)
      u.agent.combat.idealTargetDistance = 0
      u.agent.combat.breakFormationToAttack = true
    }
    def runCharge(u: FriendlyUnitInfo): Unit = {
      assert(u.ready && AttackDelay.safeToIssueOrder(u))
      charge.invoke(u.agent.combat)
    }
    check("Combat uses its existing Chase branch between attacks on a fleeing Overlord") {
      val u = unit(cooldown = 0)
      prepare(u, ahead)
      val api = u.bwapiUnit.asInstanceOf[bwapi.AttackReadinessTestUnit]
      runCharge(u)
      assert(api.commands == Vector("attack"))
      // Observe the shot after command delivery, while the same target is still in range.
      With.frame += 3
      u.lastFrameStartingAttack = With.frame - 1
      set(u, "_cooldownRaw", 8)
      set(u, "_orderTarget", Some(ahead))
      assert(u.inRangeToAttack(ahead))
      runCharge(u)
      assert(u.agent.actions.contains("Chase"))
      assert(api.commands == Vector("attack", "move"))
      // The move order clears BWAPI's attack target; the next attack must be issued again.
      With.frame += 5
      set(u, "_cooldownRaw", 3)
      set(u, "_orderTarget", None)
      runCharge(u)
      assert(api.commands == Vector("attack", "move", "attack"))
    }
    check("Combat issues a turning attack before targetless readiness") {
      val u = unit(heading = Math.PI)
      prepare(u, ahead)
      assert(!u.readyForAttackOrder)
      runCharge(u)
      assert(u.bwapiUnit.asInstanceOf[bwapi.AttackReadinessTestUnit].commands == Vector("attack"))
    }
    println(s"$passed attack readiness checks passed")
  }
}
