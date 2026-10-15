package Micro

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Actions.Combat.Tactics.Spot
import Micro.Coordination.Pathing.MicroPathing
import Performance.Cache
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object SpotTest extends App {
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

  def unit(kind: UnitClass, at: Pixel, sight: Int = 288, rangeAir: Double = 160.0, cloaked: Boolean = false): FriendlyUnitInfo = {
    val u = allocate(classOf[FriendlyUnitInfo])
    set(u, "_unitClass", kind)
    set(u, "_pixel", at)
    set(u, "_flying", kind.isFlyer)
    set(u, "_cloaked", cloaked)
    set(u, "sightRangePixelsCache", new Cache(() => sight))
    set(u, "pixelRangeAirCache", new Cache(() => rangeAir))
    u
  }

  check("closing Wraiths do not repel an Observer before it can detect them") {
    val observer = unit(Protoss.Observer, Pixel(1000, 1000))
    val wraith = unit(Terran.Wraith, Pixel(1320, 1000), cloaked = true)
    assert(!Spot.shouldRetreatFrom(observer, wraith))
    set(wraith, "_pixel", Pixel(1256, 1000))
    assert(!Spot.shouldRetreatFrom(observer, wraith), "the cap is a strict inner boundary")
    set(wraith, "_pixel", Pixel(1255, 1000))
    assert(Spot.shouldRetreatFrom(observer, wraith), "nearby danger still permits retreat")
  }
  check("upgraded detection keeps the sight cap only for cloaked threats") {
    val observer = unit(Protoss.Observer, Pixel(1000, 1000), 352)
    val wraith = unit(Terran.Wraith, Pixel(1300, 1000), rangeAir = 256.0, cloaked = true)
    assert(Spot.shouldRetreatFrom(observer, wraith))
    set(wraith, "_pixel", Pixel(1320, 1000))
    assert(!Spot.shouldRetreatFrom(observer, wraith), "long-range cloaked targets must remain detectable")
    set(wraith, "_cloaked", false)
    assert(Spot.shouldRetreatFrom(observer, wraith), "an uncloaked target needs no detection exception")
  }
  check("the Allegro cannon cannot shoot before the Observer's retreat check fires") {
    val observer = unit(Protoss.Observer, Pixel(3315, 3535))
    val cannon = unit(Protoss.PhotonCannon, Pixel(3040, 3456), rangeAir = 224.0)
    assert(observer.pixelDistanceCenter(cannon) > observer.sightPixels - 32)
    assert(observer.pixelDistanceEdge(cannon) > 224, "this replay position is still outside cannon range")
    assert(Spot.shouldRetreatFrom(observer, cannon), "retreat must start before entering cannon range")
    set(observer, "_pixel", Pixel(3220, 3705))
    set(cannon, "_pixel", Pixel(3008, 3552))
    assert(Spot.shouldRetreatFrom(observer, cannon), "the fatal approach must not be considered safe")
  }
  check("uncloaked Wraiths do not receive the detection exception") {
    val observer = unit(Protoss.Observer, Pixel(1000, 1000))
    val wraith = unit(Terran.Wraith, Pixel(1280, 1000))
    assert(Spot.shouldRetreatFrom(observer, wraith))
    set(wraith, "_cloaked", true)
    assert(!Spot.shouldRetreatFrom(observer, wraith))
  }
  check("non-detector spotters keep a weapon buffer even against cloaked enemies") {
    val wraith = unit(Terran.Wraith, Pixel(1000, 1000))
    val enemy = unit(Terran.Wraith, Pixel(1300, 1000), rangeAir = 256.0, cloaked = true)
    for (speed <- Seq(-6.0, 0.0, 6.0)) {
      set(wraith, "_velocityX", speed)
      set(enemy, "_velocityX", -speed)
      set(enemy, "_pixel", Pixel(1450, 1000))
      assert(!Spot.shouldRetreatFrom(wraith, enemy))
      set(enemy, "_pixel", Pixel(1300, 1000))
      assert(Spot.shouldRetreatFrom(wraith, enemy))
    }
  }
  check("the replay's uncovered Wraith gets a destination inside current detection") {
    val observer = unit(Protoss.Observer, Pixel(2961, 465))
    val wraith = unit(Terran.Wraith, Pixel(3175, 219), cloaked = true)
    val oldDestination = Pixel(2983, 459)
    assert(wraith.pixelDistanceCenter(oldDestination) > observer.detectionRangePixels)
    val goal = Spot.spottingPosition(observer, Some(wraith), Seq(oldDestination))
    assert(wraith.pixelDistanceCenter(goal) <= observer.detectionRangePixels - 16)
    set(observer, "_pixel", goal)
    assert(!Spot.shouldRetreatFrom(observer, wraith), "covering position must not immediately trigger retreat")
  }
  check("48-frame predictions and distant army goals cannot sacrifice current coverage") {
    val observer = unit(Protoss.Observer, Pixel(2000, 1200))
    val wraith = unit(Terran.Wraith, Pixel(2000, 1000), cloaked = true)
    for (dx <- Seq(-320, 0, 320); dy <- Seq(-320, 0, 320)) {
      val goals = Seq(wraith.pixel.add(dx, dy), Pixel(100, 2800), observer.pixel)
      val destination = Spot.spottingPosition(observer, Some(wraith), goals)
      assert(wraith.pixelDistanceCenter(destination) <= observer.detectionRangePixels - 16, (dx, dy, destination))
    }
  }
  check("spotting without a detection target keeps the existing goal selection") {
    val observer = unit(Protoss.Observer, Pixel(2000, 1200))
    val goals = Seq(Pixel(1000, 1000), Pixel(800, 1200), observer.pixel)
    assert(Spot.spottingPosition(observer, None, goals) == MicroPathing.pullTowards(observer.sightPixels, goals: _*))
  }
  check("uncloaked spotting targets do not force current detection coverage") {
    val observer = unit(Protoss.Observer, Pixel(2000, 1200))
    val wraith = unit(Terran.Wraith, Pixel(2000, 1000))
    val goals = Seq(Pixel(1000, 1000), observer.pixel)
    assert(Spot.spottingPosition(observer, Some(wraith), goals) == MicroPathing.pullTowards(observer.sightPixels, goals: _*))
  }
  println(s"$passed Spot tests passed")
}
