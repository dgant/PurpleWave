package Information.Scouting

import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech

import scala.collection.mutable

/**
  * BWAPI doesn't expose what techs a player has even if clear evidence has been shown to the player
  * This is a (very rough attempt at a) way to track those techs.
  */
trait EnemyTechs {
  lazy val techsOwned: Map[PlayerInfo, mutable.HashSet[Tech]] = With.enemies.map(e => (e, new mutable.HashSet[Tech])).toMap

  protected def updateEnemyTechs(): Unit = {
    val ours = With.units.ours

    // TODO: This logic is bogus in FFA (and needlessly slow)
    //
    With.enemies.foreach(enemy => {
      def addIf(tech: Tech, boolean: Boolean, checkOurs: Boolean = false): Unit = {
        if (checkOurs && With.self.hasTech(tech)) {
          return
        } else if (boolean && tech.race == enemy.raceCurrent) {
          techsOwned(enemy) += tech
        }
      }

      val any = With.units.playerOwned
      val enemies = With.units.enemy

      // The checks based on unit statuses aren't brilliant;
      // we could proc them on ourselves
      // or fail to proc on an enemy who missed while casting
      addIf(Terran.GhostCloak,      enemies.exists(u => Terran.Ghost(u) && u.cloaked))
      addIf(Terran.Irradiate,       any.exists(_.irradiated),     checkOurs = true)
      addIf(Terran.Lockdown,        ours.exists(_.lockedDown))
      addIf(Terran.OpticalFlare,    ours.exists(_.blind))
      addIf(Terran.SiegeMode,       enemies.exists(Terran.SiegeTankSieged))
      addIf(Terran.SpiderMinePlant, enemies.exists(Terran.SpiderMine))
      addIf(Terran.Stim,            enemies.exists(_.stimmed))
      addIf(Terran.WraithCloak,     enemies.exists(u => Terran.Wraith(u) && u.cloaked))
      addIf(Zerg.Burrow,            enemies.exists(u => u.burrowed && u.isNone(Terran.SpiderMine, Zerg.Lurker)))
      addIf(Zerg.Ensnare,           ours.exists(_.ensnared),      checkOurs = true)
      addIf(Zerg.LurkerMorph,       enemies.exists(Zerg.Lurker) || enemies.exists(Zerg.LurkerEgg))
      addIf(Zerg.Plague,            ours.exists(_.plagued),       checkOurs = true)
      addIf(Zerg.SpawnBroodlings,   enemies.exists(Zerg.Broodling))
      addIf(Protoss.Maelstrom,      ours.exists(_.maelstrommed),  checkOurs = true)
      addIf(Protoss.Stasis,         ours.exists(_.stasised),      checkOurs = true)
    })
  }
}
