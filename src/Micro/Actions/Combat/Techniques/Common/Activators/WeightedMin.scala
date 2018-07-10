package Micro.Actions.Combat.Techniques.Common.Activators

import Micro.Actions.Combat.Techniques.Common.ActionTechnique

class WeightedMin(technique: ActionTechnique) extends GeneralizedWeightedMean(technique) {
  
  // https://en.wikipedia.org/wiki/Generalized_mean

  // For a long time this was -2, but that means it's impossible to outweigh any 0.0 applicability
  // This meant, for example, that the presence of 1 enemy Dragoon very far awaywould prevent 99 Dragoons from abusing 99 Zealots
  // Hopefully this provides better results
  override protected val tension: Double = 0.5
  
}
