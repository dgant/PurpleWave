package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractCacheArray
import Lifecycle.With
import Performance.Cache

class GridOccupancy extends AbstractCacheArray[Int](i => new Cache[Int](() =>
  With.grids.units.get(i).view.map(u => if(u.flying) 0 else u.unitClass.sqrtArea).sum))