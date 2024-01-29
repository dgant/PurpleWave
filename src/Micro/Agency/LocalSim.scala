package Micro.Agency

import Lifecycle.With

class LocalSim {

  var lastFrame: Int = -1
  private var _shouldFight: Boolean = _

  class LocalSimState {

  }

  def shouldFight(): Boolean = {
    if (lastFrame < With.frame) {
      performLocalSim()
    }
    _shouldFight
  }

  private def performLocalSim(): Unit = {

  }

}
