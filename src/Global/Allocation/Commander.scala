package Global.Allocation

import Global.Allocation.Intents.Intent
import Startup.With
import bwapi.{Unit, UnitType}

import scala.collection.mutable

class Commander {
  
  val _intents = new mutable.HashMap[bwapi.Unit, Intent]
  val _nextOrderFrame = new mutable.HashMap[bwapi.Unit, Int] { override def default(key: Unit): Int = 0 }
  
  def intend(unit:bwapi.Unit, intent:Intent){
    _intents.put(unit, intent)
  }
  
  def onFrame() {
    _intents.foreach(intent => _order(intent._1, intent._2))
    _intents.clear()
  }
  
  def _order(unit:bwapi.Unit, intent:Intent) {
    
    if (_nextOrderFrame(unit) > With.game.getFrameCount) { return }
    
    unit.attack(intent.position.get)
    
    //Compensate for possibility of cancelling attack animation
    //See https://github.com/tscmoo/tsc-bwai/blob/master/src/unit_controls.h#L1569
    //and https://github.com/davechurchill/ualbertabot/blob/922966f5f1442029f811d9c6a34d9ba94fc871df/UAlbertaBot/Source/CombatData.cpp#L221
    _nextOrderFrame(unit) = With.game.getFrameCount + 4 + With.game.getLatencyFrames +
      (if (List(UnitType.Protoss_Dragoon, UnitType.Zerg_Devourer).contains(unit.getType)) 3 else 0)
  }
}
