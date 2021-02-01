package Planning.Plans.Army

import Information.Battles.Types.Division
import Information.Geography.Types.Base
import Lifecycle.With
import Planning.Prioritized

class DefendBases extends Prioritized {
  
  private lazy val baseDefensePlans = With.geography.bases.map(base => (base, new DefendBase(base))).toMap

  private case class BaseScore(base: Base) {
    lazy val defensible: Boolean = division.exists(_.enemies.nonEmpty) && (
      With.units.ours.exists(_.agent.toBuildTile.exists(_.base.contains(base)))
      || base.units.exists(unit => unit.isOurs && unit.unitClass.isBuilding && ! unit.flying))
    lazy val division: Option[Division] = With.battles.divisions.find(_.bases.contains(base))

    // Prioritize fighting the biggest enemy division, to avoid getting rolled by it
    lazy val score: Double = division.map(_.enemies.view.map(_.subjectiveValue).sum).sum
  }
  
  def update() {

    val zoneScores = baseDefensePlans.keys.map(BaseScore).filter(_.defensible)
    
    if (zoneScores.isEmpty) return

    zoneScores
        .groupBy(_.division.get)
        .toVector
        .sortBy(-_._2.map(_.score).sum)
        .foreach(baseGroup => {
          // When we have multiple viable defense plans for a division,
          // delegate to an arbitrary-but-preferably-stable plan for that division.
          val baseScore = baseGroup._2.minBy(_.base.hashCode)
          val defensePlan = baseDefensePlans(baseScore.base)
          defensePlan.enemies = baseScore.division.get.enemies.view.flatMap(_.foreign).toSeq
          defensePlan.update()
      })
  }
}
