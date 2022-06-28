package Planning.Plans.GamePlans.All

import Utilities.DoQueue

abstract class EvEFFA extends GameplanImperative {
  def getArmy()       : Unit
  def getProduction() : Unit
  def getTech()       : Unit
  def getDetection()  : Unit
  def getExpansion()  : Unit
  override def executeMain(): Unit = {
    val goArmy        = new DoQueue(getArmy)
    val goProduction  = new DoQueue(getProduction)
    val goTech        = new DoQueue(getTech)
    val goDetect      = new DoQueue(getDetection)
    val goExpand      = new DoQueue(getExpansion)
  }
}
