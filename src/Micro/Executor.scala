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
    val awakeIntentions = intentions.values.filter(intent => With.commander.readyForCommand(intent.unit))
    awakeIntentions.foreach(intent => intent.behavior.execute(intent))
    lastIntentions = intentions
    intentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  }
}
