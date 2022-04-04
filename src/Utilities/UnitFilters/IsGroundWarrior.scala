package Utilities.UnitFilters

object IsGroundWarrior extends IsAll(IsWarrior, ! _.flying)
