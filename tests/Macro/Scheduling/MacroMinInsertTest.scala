package Macro.Scheduling

import Lifecycle.With
import Macro.Requests.{RequestBuildable, RequestUnit}
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}

import scala.collection.mutable

object MacroMinInsertTest extends App {
  val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
  unsafeField.setAccessible(true)
  val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
  With.proxy = unsafe.allocateInstance(classOf[ProxyBWAPI]).asInstanceOf[ProxyBWAPI]
  val classesField = classOf[ProxyBWAPI].getDeclaredField("unitClasses")
  classesField.setAccessible(true)
  classesField.set(With.proxy, UnitTypes.all.map(UnitClass))
  val actual = new MacroSim().minInsert
  val original = new mutable.OpenHashMap[RequestBuildable, Int]
  var previous = Vector.empty[RequestUnit]
  (0 until 4096).foreach { frame =>
    actual.clear()
    original.clear()
    previous.foreach(request => assert(actual.get(request) == original.get(request)))
    val requests = Vector.tabulate(32)(i => RequestUnit(Protoss.Dragoon, frame * 32 + i))
    requests.zipWithIndex.foreach { case (request, i) =>
      assert(actual.getOrElse(request, 0) == original.getOrElse(request, 0))
      Seq(i, i + 3, i + 1).foreach { step =>
        actual(request) = Math.max(step, actual.getOrElse(request, step))
        original(request) = Math.max(step, original.getOrElse(request, step))
        assert(actual.get(request.copy()) == original.get(request.copy()))
        assert(actual.get(request).exists(_ > i) == original.get(request).exists(_ > i))
      }
    }
    assert(actual.size == original.size)
    // The autosupply request also clears these lower bounds within a simulation.
    if (frame % 7 == 0) {
      actual.clear()
      original.clear()
      requests.foreach(request => assert(actual.getOrElse(request, 0) == original.getOrElse(request, 0)))
    }
    previous = requests
  }
  println("PASS insertion bounds match through fresh requests, equal keys, updates, and resets")

  // Inspect storage to catch the original growth with cumulative deleted keys.
  def capacity(map: AnyRef): Int = {
    var owner: Class[_] = map.getClass
    while (owner != null) {
      owner.getDeclaredFields.find(f => f.getType.isArray && f.getName.toLowerCase.endsWith("table")) match {
        case Some(field) =>
          field.setAccessible(true)
          return java.lang.reflect.Array.getLength(field.get(map))
        case None => owner = owner.getSuperclass
      }
    }
    throw new AssertionError("Map backing table not found")
  }
  assert(capacity(actual) <= 256, "Storage grew with historical requests despite at most 32 live keys")
  println("PASS reset-heavy request map keeps bounded storage: " + capacity(actual) + " slots")
}
