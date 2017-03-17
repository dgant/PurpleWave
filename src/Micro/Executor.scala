package Micro

import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Startup.With

import scala.collection.mutable

class Executor {
  
  private var intentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  var lastIntentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  
  def intend(intention:Intention) = intentions.put(intention.unit, intention)
  
  def onFrame() {
    val awakeIntentions = intentions.filter(pair => With.commander.readyForCommand(pair._1))
    awakeIntentions.foreach(pair => pair._2.command.execute(pair._2))
    lastIntentions --= lastIntentions.keys.filterNot(_.alive)
    awakeIntentions.foreach(pair => lastIntentions.put(pair._1, pair._2))
    intentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  }
}
