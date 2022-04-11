package Placement.Access

import Placement.Templating.PlaceLabel

trait RequireLabel {
  def require: Seq[PlaceLabel]
}
