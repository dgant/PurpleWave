package Utilities.UnitFilters

object IsMechWarrior extends IsAll(IsWarrior, _.unitClass.isMechanical)
