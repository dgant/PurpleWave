package Utilities.UnitFilters

import ProxyBwapi.Races.Terran

object IsBioWarrior extends IsAll(IsAny(Terran.Marine, Terran.Firebat), _.aliveAndComplete, _.canMove)
