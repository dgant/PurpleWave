package Utilities

import Startup.With

case object EnrichUnit {
  implicit class EnrichedUnit(unit: bwapi.Unit) {
    def isOurs              : Boolean = { unit.getPlayer == With.game.self }
    def isFriendly          : Boolean = { isOurs || unit.getPlayer.isAlly(With.game.self) }
    def isEnemy             : Boolean = { unit.getPlayer.isEnemy(With.game.self) }
    def canFight            : Boolean = { unit.isCompleted && unit.canAttack }
    def totalHealth         : Int     = { unit.getHitPoints + unit.getShields }
    def initialTotalHealth  : Int     = { unit.getInitialHitPoints + unit.getType.maxShields }
    def range               : Int     = { List(unit.getType.groundWeapon.maxRange, unit.getType.airWeapon.maxRange).max }
    def isEnemyOf(otherUnit:bwapi.Unit): Boolean = { unit.getPlayer.isEnemy(otherUnit.getPlayer) }
  }
}
