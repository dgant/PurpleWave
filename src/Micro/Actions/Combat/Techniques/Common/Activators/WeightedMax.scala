package Micro.Actions.Combat.Techniques.Common.Activators

import Micro.Actions.Combat.Techniques.Common.ActionTechnique

class WeightedMax(technique: ActionTechnique) extends GeneralizedWeightedMean(technique) {
  
    // https://en.wikipedia.org/wiki/Generalized_mean
  override protected val tension: Double = 2
  
}
