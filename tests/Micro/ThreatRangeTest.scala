package Micro

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Matchups.MatchupAnalysis
import Performance.Cache
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object ThreatRangeTest extends App {
  val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
  unsafeField.setAccessible(true)
  val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
  def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
  def set(obj: AnyRef, name: String, value: Any): Unit = {
    val fields = Iterator.iterate[Class[_]](obj.getClass)(_.getSuperclass).takeWhile(_ != null).flatMap(_.getDeclaredFields)
    val field = fields.find(f => f.getName == name || f.getName.endsWith("$$" + name)).get
    field.setAccessible(true)
    field.set(obj, value.asInstanceOf[AnyRef])
  }
  var passed = 0
  def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }

  With.frame = 100
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))

  def unit(kind: UnitClass, at: Pixel, complete: Boolean = true): FriendlyUnitInfo = {
    val u = allocate(classOf[FriendlyUnitInfo])
    set(u, "_unitClass", kind)
    set(u, "_pixel", at)
    set(u, "_complete", complete)
    set(u, "canAttackCache", new Cache(() => complete))
    set(u, "canBeAttackedCache", new Cache(() => true))
    set(u, "_effectivelyCloaked", new Cache(() => false))
    set(u, "pixelRangeGroundCache", new Cache(() => kind.pixelRangeGround))
    u
  }
  def matchups(me: UnitInfo, dangers: UnitInfo*): MatchupAnalysis = new MatchupAnalysis(me) {
    override def threats: Seq[UnitInfo] = dangers
  }

  check("an unfinished cannon cannot hide completed cannon range on Allegro") {
    val dragoon = unit(Protoss.Dragoon, Pixel(3151, 3759))
    val completed = unit(Protoss.PhotonCannon, Pixel(2944, 3808))
    val unfinished = unit(Protoss.PhotonCannon, Pixel(2976, 3680), complete = false)
    val analysis = matchups(dragoon, completed, unfinished)
    assert(completed.inRangeToAttack(dragoon), "the completed cannon can already shoot")
    assert(unfinished.pixelsToGetInRange(dragoon) > 32000, "incomplete attack queries return a sentinel")
    assert(analysis.threatDeepest.exists(_ eq unfinished), "the unfinished cannon is geometrically deeper")
    assert(analysis.pixelsToThreatRange.exists(_ < 0), "micro must not treat the approach as safe")
  }
  check("the original Zealot and Dragoon approaches cannot hide the rear cannon") {
    val positions = Seq(
      (Protoss.Zealot, Pixel(3103, 3650), Pixel(2976, 3488)),
      (Protoss.Dragoon, Pixel(3119, 3685), Pixel(3008, 3552)))
    for ((kind, at, constructionAt) <- positions) {
      val attacker = unit(kind, at)
      val completed = unit(Protoss.PhotonCannon, Pixel(3040, 3456))
      val unfinished = unit(Protoss.PhotonCannon, constructionAt, complete = false)
      val analysis = matchups(attacker, completed, unfinished)
      assert(completed.inRangeToAttack(attacker))
      assert(analysis.threatDeepest.exists(_ eq unfinished))
      assert(analysis.pixelsToThreatRange.exists(_ < 0))
    }
  }
  check("a soon-complete threat keeps its future weapon range") {
    val dragoon = unit(Protoss.Dragoon, Pixel(3151, 3759))
    val cannon = unit(Protoss.PhotonCannon, Pixel(2976, 3680), complete = false)
    assert(matchups(dragoon, cannon).pixelsToThreatRange.exists(_ < 0))
  }
  check("completed threats retain their signed range gap") {
    val cannon = unit(Protoss.PhotonCannon, Pixel(1000, 1000))
    for (x <- Seq(1100, 1500)) {
      val dragoon = unit(Protoss.Dragoon, Pixel(x, 1000))
      assert(matchups(dragoon, cannon).pixelsToThreatRange.contains(cannon.pixelsToGetInRange(dragoon)))
    }
  }
  check("no threats means no range restriction") {
    assert(matchups(unit(Protoss.Dragoon, Pixel(1000, 1000))).pixelsToThreatRange.isEmpty)
  }
  println(s"$passed threat range tests passed")
}
