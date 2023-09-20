package Information.Geography.Calculations

import Information.Geography.Types.{Edge, Geo}

object GetExits {
  def apply(geos: Iterable[Geo]): Set[Edge] = {
    geos
      .view
      .flatMap(_.edges)
      .filter(_.zones.exists(z => ! geos.view.flatMap(_.zones).exists(z==)))
      .toSet
  }
}
