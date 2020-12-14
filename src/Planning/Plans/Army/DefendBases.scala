package Planning.Plans.Army

import Information.Battles.Types.Division
import Information.Geography.Types.Base
import Lifecycle.With
import Planning.Plan

class DefendBases extends Plan {
  
  private lazy val bases = With.geography.bases.map(base => (base, new DefendBase(base))).toMap
  
  override def getChildren: Iterable[Plan] = bases.values

  private case class BaseScore(base: Base) {
    lazy val defensible: Boolean = division.exists(_.enemies.nonEmpty) && (
      With.units.ours.exists(_.agent.toBuildTile.exists(_.base.contains(base)))
      || base.units.exists(unit => unit.isOurs && unit.unitClass.isBuilding && ! unit.flying))
    lazy val division: Option[Division] = With.battles.divisions.find(_.bases.contains(base))

    // Prioritize fighting the biggest enemy division, to avoid getting rolled by it
    lazy val score: Double = division.map(_.enemies.view.map(_.subjectiveValue).sum).sum
  }
  
  protected override def onUpdate() {

    val zoneScores = bases.keys.map(BaseScore).filter(_.defensible)
    
    if (zoneScores.isEmpty) {
      return
    }

    // For each
    zoneScores
        .groupBy(_.division.get)
        .toVector
        .sortBy(-_._2.map(_.score).sum)
        .foreach(baseGroup => {
          // Take an arbitrary-but-preferably-stable base from the group
          val baseScore = baseGroup._2.minBy(_.base.hashCode)
          val defensePlan = bases(baseScore.base)
          defensePlan.enemies = baseScore.division.get.enemies.view.flatMap(_.foreign).toSeq
          delegate(defensePlan)
      })
  }
}
