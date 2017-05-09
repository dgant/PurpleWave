package Planning.Plans.Information

import Lifecycle.With
import Planning.Composition.Property
import Planning.Plan
import bwapi.Race

class SwitchEnemyRace extends Plan {
  val terran  = new Property[Plan](new Plan)
  val protoss = new Property[Plan](new Plan)
  val zerg    = new Property[Plan](new Plan)
  val random  = new Property[Plan](new Plan)
  
  override def getChildren: Iterable[Plan] = Vector(terran.get, protoss.get, zerg.get, random.get)
  
  private val mysteriousRaces = Vector(Race.Random, Race.Unknown, Race.None)
  
  private var permanentRace:Option[Race] = None
  
  override def update() {
    
    var knownRace = permanentRace.orElse(With.enemies.headOption.map(_.race)).getOrElse(Race.Random)
    
    if (mysteriousRaces.contains(knownRace)) {
      knownRace = With.units.enemy.map(_.unitClass.race).find(unitRace => ! mysteriousRaces.contains(unitRace)).getOrElse(Race.Random)
    }
    
    if ( ! mysteriousRaces.contains(knownRace)) {
      permanentRace = Some(knownRace)
    }
    
    val matchupPlan = knownRace match {
      case Race.Terran    => terran
      case Race.Protoss   => protoss
      case Race.Zerg      => zerg
      case _              => random
    }
      
    matchupPlan.get.update()
  }
}
