package Placement.Access

import Placement.Templating.PlaceLabel

trait PreferLabel {
  def prefer: Seq[PlaceLabel]
}
