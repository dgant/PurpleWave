import glob
import itertools
import json
import os
import shutil
import sys
import traceback

scbw_root = os.path.join('c:\\', 'users', 'd', 'appdata', 'roaming', 'scbw')
games_root = os.path.join(scbw_root, 'games')
losses_dir = os.path.join(scbw_root, 'losses')
wins_dir   = os.path.join(scbw_root, 'wins')
foes = {}
maps = {}

class Matchup:
  def __init__(self, name):
    self.name = name
    self.games = []
  def wins(self):
    return len(self.won_games())
  def losses(self):
    return len(self.lost_games())
  def conclusive(self):
    return self.total() - self.inconclusive()
  def inconclusive(self):
    return len(self.inconclusive_games())  
  def total(self):
    return len(self.games)
  def won_games(self):
    return [game for game in self.games if game.won()]
  def lost_games(self):
    return [game for game in self.games if game.lost()]
  def inconclusive_games(self):
    return [game for game in self.games if game.inconclusive()]
  def winrate(self):
    return 0.0 if (self.conclusive() == 0) else self.wins() / self.conclusive()
    
class Game:
  def __init__(self, game_id, full_directory, timestamp, game_json):
    self.id         = game_id
    self.directory  = full_directory
    self.timestamp  = timestamp
    self.json       = game_json    
    self.map        = game_json['map_name']
    self.me         = game_json['bots'][0]
    self.foe        = game_json['bots'][1]
    self.crashed    = game_json['is_crashed']
    self.duration   = game_json['game_time']
    self.winner     = game_json['winner']
    self.loser      = game_json['loser']
    self.ran        = os.path.isfile(self.replay(0)) and os.path.isfile(self.replay(1)) and len(os.listdir(os.path.join(self.directory, 'write_0'))) > 0
  def won(self):
    return self.ran and self.winner is not None and self.me is not None and self._names_match(self.winner, self.me)
  def lost(self):
    return self.ran and self.winner is not None and self.me is not None and self._names_match(self.winner, self.foe)
  def inconclusive(self):
    return not self.ran or not (self.won() or self.lost())
  def replay(self, player=0):
    return os.path.join(self.directory, 'player_' + str(player) + '.rep')
  def _names_match(self, a, b):
    a = a.lower()
    b = b.lower()
    return len(a) > 0 and len(b) > 0 and (a in b or b in a)
    
def count_game(game_id):
  try:
    full_directory = os.path.join(games_root, game_id)
    full_path = os.path.join(full_directory, 'result.json')
    with open(full_path) as result_file:
      timestamp = os.path.getmtime(full_path)
      game = Game(game_id, full_directory, timestamp, json.load(result_file))
      foes.setdefault(game.foe, Matchup(game.foe)).games.append(game)
      maps.setdefault(game.map, Matchup(game.map)).games.append(game)
  except FileNotFoundError: pass
  except Exception as e:
    traceback.print_exc()
    print()

def print_matchups(matchup_dict):
  matchups = list(matchup_dict.values())
  games = list(itertools.chain.from_iterable([matchup.games for matchup in matchups]))
  matchups.sort(key=lambda x: x.winrate())
  matchups.append(Matchup("Overall"))
  matchups[-1].games = games
  
  left_width = 1 + max([len(matchup.name) for matchup in matchups])
  
  for matchup in matchups:
    print('{} {} ({} - {} - {} of {})'.format(
    '{}:'.format(matchup.name.ljust(left_width)),
    "{:.0%}".format(matchup.winrate())  .ljust(4),
    str(matchup.wins())                 .ljust(3),
    str(matchup.losses())               .ljust(3),
    str(matchup.inconclusive())         .ljust(3),
    str(matchup.total())))

def copy_games():
  if len(losses_dir)  < 20: raise Exception('Suspicious replay directory!' + losses_dir)
  if len(wins_dir)    < 20: raise Exception('Suspicious replay directory!' + wins_dir)
  winloss_dirs = [losses_dir, wins_dir]
  for dir in winloss_dirs: os.makedirs(dir, exist_ok=True)
  for dir in winloss_dirs:
    for file in os.listdir(dir):
      os.remove(os.path.join(dir, file))
  matchups = foes.values()
  games = list(itertools.chain.from_iterable([matchup.games for matchup in matchups]))
  games.sort(key=lambda x: x.timestamp)
  for game in games:
    try:
      write_directory = os.path.join(game.directory, 'write_0')
      logs_directory  = os.path.join(game.directory, 'logs_0')
      active_dir      = losses_dir if game.lost() else wins_dir
      filename_base   = os.path.join(active_dir, '{}-{}-{}'.format(game.timestamp, game.foe, game.id))
      filename_replay = filename_base + '.rep'
      filename_csv    = filename_base + '.csv' # PW-specific data
      filename_bot    = filename_base + ".bot.txt" # PW-specific data
      filename_log    = filename_base + ".log.txt" # PW-specific data
      filename_pwcs   = filename_base + ".pwcs" #PW-specific data
      shutil.copyfile(game.replay(), filename_replay)
      for written_file in os.listdir(write_directory):
        if '.csv' in written_file:
          shutil.copyfile(os.path.join(write_directory, written_file), filename_csv)
        elif '.log.txt' in written_file:
          shutil.copyfile(os.path.join(write_directory, written_file), filename_log)
        elif '.pwcs' in written_file:
          shutil.copyfile(os.path.join(write_directory, written_file), filename_pwcs)
        elif '.mapbin' in written_file:
          shutil.copyfile(os.path.join(write_directory, written_file), os.path.join(active_dir, written_file))
      for logged_file in os.listdir(logs_directory):
        if 'bot.log' in logged_file:
          shutil.copyfile(os.path.join(logs_directory, logged_file), filename_bot)        
    except:
      print("Error copying " + game.directory + ":", sys.exc_info()[1])
  
def main():
  for game_id in os.listdir(games_root):
    count_game(game_id)
  print_matchups(foes)
  print_matchups(maps)
  copy_games()

main()
