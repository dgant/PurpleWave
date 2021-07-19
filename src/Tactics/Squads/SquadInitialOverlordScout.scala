package Tactics.Squads

import Lifecycle.With
import Micro.Agency.Intention
import Performance.Cache
import Planning.UnitMatchers.{MatchAnd, MatchComplete}
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class SquadInitialOverlordScout extends Squad {
  var endScouting: Boolean = false

  private val bases = new Cache(() =>
    if (With.scouting.firstEnemyMain.isDefined) {
      // Vs Terran:  Scout least-claimed known main + its natural
      // Vs Protoss: Scout least-claimed known main + its natural
      // Vs Zerg:    Scout least-claimed known bases
      // Vs Random:  Scout least-claimed known main (to determine race)
      // TODO LATER: Then scout bases nearest opponent
      val main = With.scouting.firstEnemyMain.get
      if (With.enemy.isTerran || With.enemy.isProtoss) {
        Seq(main) ++ main.natural
      } else if (With.enemy.isZerg) {
        (Seq(main) ++ With.geography.enemyBases).distinct
      } else Seq(main)
    } else {
      // Vs Terran:  Scout main with nearest scout
      // Vs Protoss: Scout main's natural with nearest scout
      // Vs Zerg:    Scout main with nearest scout
      // Vs Random:  Scout main with nearest scout

      // Sort by ENTRANCE distance to encourage seeing critical buildings and flying over armies leaving the base
      val candidateBases = With.geography.startBases.filterNot(_.owner.isUs).filterNot(_.scouted)
      candidateBases.sortBy(main => {
        val base = main.natural.filter(!_.scouted && With.enemy.isProtoss).getOrElse(main)
        base.zone.exit
          .map(_.pixelCenter)
          .getOrElse(base.townHallArea.center)
          .pixelDistance(
            units
              .headOption
              .map(_.pixel)
              .getOrElse(With.geography.home.center))
      })
    })

  def launch(): Unit = {
    if ( ! With.self.isZerg) return

    endScouting ||= With.units.existsEnemy(Terran.Marine, Terran.Goliath, Terran.Wraith)
    endScouting ||= With.units.existsEnemy(MatchAnd(MatchComplete, Terran.Barracks))
    endScouting ||= With.units.existsEnemy(MatchAnd(MatchComplete, Terran.Starport))
    endScouting ||= With.units.existsEnemy(Protoss.Dragoon, Protoss.Corsair)
    endScouting ||= With.units.existsEnemy(MatchAnd(MatchComplete, Protoss.Stargate))
    endScouting ||= With.units.existsEnemy(MatchAnd(MatchComplete, Protoss.CyberneticsCore))
    endScouting ||= With.units.existsEnemy(Zerg.Mutalisk, Zerg.Hydralisk, Zerg.Scourge)
    endScouting ||= With.units.existsEnemy(MatchAnd(MatchComplete, Zerg.HydraliskDen))
    endScouting ||= With.units.existsEnemy(Zerg.Spire)
    if (endScouting) {
      lock.release()
      return
    }
    if (bases().isEmpty) return

    vicinity = bases().head.townHallArea.center
    lock.preference = PreferClose(vicinity)
    lock.acquire()
  }

  def run(): Unit = {
    if (bases().isEmpty) return
    units.zipWithIndex.foreach(s => {
      val overlord = s._1
      val i = s._2
      val base = bases()(i % bases().size)
      val goal = base.townHallArea.tiles.minBy(overlord.pixelDistanceTravelling)
      overlord.intend(this, new Intention {
        toTravel = Some(goal.center)
        toScoutTiles = if (goal.explored) base.tiles.view.filter(_.buildable).toVector else Seq.empty
      })
    })
  }
}
