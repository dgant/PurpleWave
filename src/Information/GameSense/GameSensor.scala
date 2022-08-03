package Information.GameSense

import Information.GameSense.GameSenses._
import Lifecycle.With
import Performance.Tasks.TimedTask

import scala.collection.mutable

class GameSensor extends TimedTask with EconomicModel {

  EconAdvantage.setOpposite(EconDisadvantage)
  EconDisadvantage.setOpposite(EconAdvantage)
  MuscleAdvantage.setOpposite(MuscleDisadvantage)
  MuscleDisadvantage.setOpposite(MuscleAdvantage)
  MapControl.setOpposite(MapContained)
  MapContained.setOpposite(MapControl)

  private case class GameSenseEvent(sense: GameSense, untilFrame: Int)

  private val currentEvents = new mutable.HashMap[GameSense, GameSenseEvent]

  def senses: Iterable[GameSense] = currentEvents.keys

  private var lastUpdate: Int = 0

  override protected def onRun(budgetMs: Long): Unit = {
    updateEconomicModel()
    detectEvents()
    currentEvents --= currentEvents.filter(_._2.untilFrame <= With.frame).keys
    lastUpdate = With.frame
  }

  private def detectEvents(): Unit = {

  }

  def remove(sense: GameSense): Unit = {
    currentEvents.remove(sense)
  }

  def spawnEvent(event: GameSenseEvent): Unit = {
    if (event.untilFrame <= With.frame) return
    val previous = currentEvents.get(event.sense)
    val canonical = (previous.toSeq :+ event).maxBy(_.untilFrame)

    currentEvents(event.sense) = canonical
    canonical.sense.opposite.foreach(currentEvents.remove)
  }
}
