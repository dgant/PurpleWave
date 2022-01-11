package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Views.View
import Lifecycle.With
import Micro.Formation.FormationStandard

object ShowFormations extends View {

  override def renderMap() {
    With.squads.all
      .filter(_.units.nonEmpty)
      .foreach(
        _.formations
        .filter(_.isInstanceOf[FormationStandard])
        .map(_.asInstanceOf[FormationStandard])
        .foreach(_.renderMap()))
  }
}
