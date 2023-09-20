package Information.Geography.Calculations

import Information.Geography.Types.{Geo, Zone}
import Lifecycle.With

object ZonesConnecting {
  def apply(a: Geo, b: Geo): Seq[Zone] = {
    With.paths.aStar(a.heart, b.heart).tiles.getOrElse(Seq.empty).map(_.zone)
  }
}
