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

  def unit(kind: UnitClass, at: Pixel, sight: Int = 288, rangeAir: Double = 160.0): FriendlyUnitInfo = {
    val u = allocate(classOf[FriendlyUnitInfo])
    set(u, "_unitClass", kind)
    set(u, "_pixel", at)
    set(u, "_flying", kind.isFlyer)
    set(u, "sightRangePixelsCache", new Cache(() => sight))
    set(u, "pixelRangeAirCache", new Cache(() => rangeAir))
    u
  }

  check("closing Wraiths do not repel an Observer before it can detect them") {
    val observer = unit(Protoss.Observer, Pixel(1000, 1000))
    val wraith = unit(Terran.Wraith, Pixel(1320, 1000))
    assert(!Spot.shouldRetreatFrom(observer, wraith))
    set(wraith, "_pixel", Pixel(1256, 1000))
    assert(!Spot.shouldRetreatFrom(observer, wraith), "the cap is a strict inner boundary")
    set(wraith, "_pixel", Pixel(1255, 1000))
    assert(Spot.shouldRetreatFrom(observer, wraith), "nearby danger still permits retreat")
  }
  check("upgraded sight only expands retreat distance against sufficiently long-range threats") {
    val observer = unit(Protoss.Observer, Pixel(1000, 1000), 352)
    val wraith = unit(Terran.Wraith, Pixel(1300, 1000))
    val goliath = unit(Terran.Goliath, Pixel(1300, 1000), rangeAir = 256.0)
    assert(!Spot.shouldRetreatFrom(observer, wraith), "short weapon range must limit the buffer")
    assert(Spot.shouldRetreatFrom(observer, goliath))
    set(goliath, "_pixel", Pixel(1320, 1000))
    assert(!Spot.shouldRetreatFrom(observer, goliath), "sight must cap the longer weapon range")
    set(wraith, "_pixel", Pixel(1255, 1000))
    assert(Spot.shouldRetreatFrom(observer, wraith))
    set(wraith, "_pixel", Pixel(1256, 1000))
    assert(!Spot.shouldRetreatFrom(observer, wraith))
  }
  check("non-detector spotters use the same distance buffer regardless of velocity") {
    val wraith = unit(Terran.Wraith, Pixel(1000, 1000))
    val enemy = unit(Terran.Wraith, Pixel(1320, 1000))
    for (speed <- Seq(-6.0, 0.0, 6.0)) {
      set(wraith, "_velocityX", speed)
      set(enemy, "_velocityX", -speed)
      set(enemy, "_pixel", Pixel(1320, 1000))
      assert(!Spot.shouldRetreatFrom(wraith, enemy))
      set(enemy, "_pixel", Pixel(1255, 1000))
      assert(Spot.shouldRetreatFrom(wraith, enemy))
    }
  }
  check("the replay's uncovered Wraith gets a destination inside current detection") {
    val observer = unit(Protoss.Observer, Pixel(2961, 465))
    val wraith = unit(Terran.Wraith, Pixel(3175, 219))
    val oldDestination = Pixel(2983, 459)
    assert(wraith.pixelDistanceCenter(oldDestination) > observer.detectionRangePixels)
    val goal = Spot.spottingPosition(observer, Some(wraith), Seq(oldDestination))
    assert(wraith.pixelDistanceCenter(goal) <= observer.detectionRangePixels - 16)
    set(observer, "_pixel", goal)
    assert(!Spot.shouldRetreatFrom(observer, wraith), "covering position must not immediately trigger retreat")
  }
  check("48-frame predictions and distant army goals cannot sacrifice current coverage") {
    val observer = unit(Protoss.Observer, Pixel(2000, 1200))
    val wraith = unit(Terran.Wraith, Pixel(2000, 1000))
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
  println(s"$passed Spot tests passed")
}
