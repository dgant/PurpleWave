package Macro.Scheduling.SmartQueue

import Macro.BuildRequests.BuildRequest

class SmartQueueItem(
  val add   : Option[BuildRequest] = None,
  val pump  : Option[BuildRequest] = None,
  val dump  : Option[BuildRequest] = None)