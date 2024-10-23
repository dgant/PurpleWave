package Macro.Actions

import Lifecycle.With
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.CountMap

import scala.collection.mutable

class Compositor extends MacroActions {
  val need      = new mutable.HashMap[UnitClass, Double].withDefaultValue(1.0)
  val goal      = new CountMap[UnitClass]
  val cap       = new CountMap[UnitClass](400)
  val current   = new CountMap[UnitClass]
  val produced  = new CountMap[UnitClass]

  def reset(): Unit = {
    need.clear()
    goal.clear()
    cap.clear()
  }
  def setNeed(c: UnitClass, v: Double): Unit = need(c) = v
  def setGoal(c: UnitClass, v: Double): Unit = goal(c) = v.toInt
  def setCap(c: UnitClass,  v: Double): Unit = cap(c) = v.toInt
  def capGoal(c: UnitClass, v: Double): Unit = { setGoal(c, v); setCap(c, v) }
  def include(c: UnitClass)           : Unit = need.getOrElseUpdate(c, 1)

  def produceNeeds(): Unit = {
    val classes = need.keys.filter(need(_) > 0)
    classes.foreach(c => {
      goal(c)     = Math.min(goal(c), cap(c))
      current(c)  = With.units.countOurs(c)
      produced(c) = current(c)
    })
    while (classes.exists(c => goal(c) > produced(c))) {
      val next = classes.maxBy(c => need(c) * (goal(c) - produced(c)))
      produced(next) += 1
      get(produced(next), next)
    }
  }
  def pumpNeeds(): Unit = {
    need.toVector.sortBy(-_._2).map(_._1).foreach(c => pump(c, cap.getOrElse(c, 400)))
  }
  def produceAndPump(): Unit = {
    produceNeeds()
    pumpNeeds()
  }
}