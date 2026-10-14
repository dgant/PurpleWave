package bwapi {
  class AttackMoveTestUnit extends Unit(null, 0, null) {
    var commands: Vector[Position] = Vector.empty
    var previous: UnitCommand = _
    override def getLastCommand(): UnitCommand = previous
    override def attack(to: Position): Boolean = {
      commands :+= to
      previous = UnitCommand.attack(this, to)
      true
    }
  }
}

package Micro {
  import Lifecycle.With
  import Lifecycle.Configure.Configuration
  import Mathematics.Points.Pixel
  import Micro.Agency.Commander
  import Performance.{Cache, Latency, ReactionTimes}
  import ProxyBwapi.{Orders, ProxyBWAPI}
  import ProxyBwapi.Races.Protoss
  import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
  import ProxyBwapi.UnitInfo.FriendlyUnitInfo
  import ProxyBwapi.UnitTracking.UnitTracker

  object AttackMoveTest extends App {
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
    With.mapPixelWidth = 4096; With.mapPixelHeight = 4096
    With.proxy = allocate(classOf[ProxyBWAPI])
    set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
    With.units = new UnitTracker
    With.configuration = new Configuration
    With.latency = allocate(classOf[Latency])
    set(With.latency, "_remainingFrames", 3)
    set(With.latency, "_turnSize", 1)
    With.reaction = new ReactionTimes { override def sluggishness: Int = 0 }
    val destination = Pixel(1807, 655)
    val attackMove = Commander.getClass.getDeclaredMethod("attackMove", classOf[FriendlyUnitInfo], classOf[Pixel])
    attackMove.setAccessible(true)

    def unit(order: String, target: Option[Pixel], prior: String = "attack-move", goal: Pixel = destination): FriendlyUnitInfo = {
      val u = allocate(classOf[FriendlyUnitInfo])
      val api = allocate(classOf[bwapi.AttackMoveTestUnit])
      api.commands = Vector.empty
      api.previous = prior match {
        case "attack-move" => bwapi.UnitCommand.attack(api, goal.bwapi)
        case "queued" => bwapi.UnitCommand.attack(api, goal.bwapi, true)
        case "attack-unit" => bwapi.UnitCommand.attack(api, api)
        case "move" => bwapi.UnitCommand.move(api, goal.bwapi)
        case _ => null
      }
      set(u, "bwapiUnit", api)
      set(u, "_unitClass", Protoss.Dragoon)
      set(u, "_pixel", Pixel(1700, 650))
      // Isolate command dispatch from terrain routing, which has its own integration tests.
      set(u, "_flying", true)
      set(u, "friendly", Some(u))
      set(u, "_squad", None); set(u, "_transport", None)
      set(u, "_order", order); set(u, "_orderTarget", None); set(u, "_orderTargetPixel", target)
      set(u, "_loadedUnitsCache", new Cache(() => Vector.empty[FriendlyUnitInfo]))
      u
    }
    def commandCount(u: FriendlyUnitInfo, to: Pixel = destination): Int = {
      attackMove.invoke(Commander, u, to)
      u.bwapiUnit.asInstanceOf[bwapi.AttackMoveTestUnit].commands.size
    }
    check("native AttackUnit keeps the original nearby attack-move goal, not the enemy position") {
      val u = unit(Orders.AttackUnit, Some(Pixel(2200, 1000)), goal = Pixel(1806, 623))
      assert(commandCount(u) == 0)
      assert(u.sleepUntil == With.frame + 4)
    }
    check("an unchanged AttackMove remains deduplicated") {
      assert(commandCount(unit(Orders.AttackMove, Some(destination))) == 0)
    }
    check("a changed attack-move goal is issued immediately even if the enemy is near the new goal") {
      assert(commandCount(unit(Orders.AttackUnit, Some(destination), goal = Pixel(1400, 655))) == 1)
    }
    check("a nearby explicitly ordered target does not stand in for an attack-move") {
      assert(commandCount(unit(Orders.AttackUnit, Some(destination), prior = "attack-unit")) == 1)
    }
    check("the existing strict 128-pixel goal tolerance is preserved") {
      assert(commandCount(unit(Orders.AttackUnit, None), destination.add(127, 0)) == 0)
      assert(commandCount(unit(Orders.AttackUnit, None), destination.add(128, 0)) == 1)
    }
    check("moves, idle orders, and stopped units are not mistaken for active attack-moves") {
      for (order <- Vector(Orders.Move, Orders.Stop, Orders.PlayerGuard)) {
        assert(commandCount(unit(order, Some(destination))) == 1)
      }
      assert(commandCount(unit(Orders.AttackUnit, Some(destination), prior = "move")) == 1)
    }
    check("missing and queued prior commands do not suppress a new immediate order") {
      for (prior <- Vector("none", "queued")) {
        assert(commandCount(unit(Orders.AttackUnit, Some(destination), prior = prior)) == 1)
      }
    }
    check("stuck-unit recovery still reissues the order") {
      val u = unit(Orders.AttackUnit, Some(destination))
      set(u, "_framesFailingToMove", 25)
      assert(commandCount(u) == 1)
    }
    check("units still sleeping receive no command") {
      val u = unit(Orders.Move, None)
      u.sleepUntil = With.frame + 1
      assert(commandCount(u) == 0)
    }
    println(s"$passed attack-move checks passed")
  }
}
