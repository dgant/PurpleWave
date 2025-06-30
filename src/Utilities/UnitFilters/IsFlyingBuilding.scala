package Utilities.UnitFilters

object IsFlyingBuilding extends IsAll(IsBuilding, _.flying)