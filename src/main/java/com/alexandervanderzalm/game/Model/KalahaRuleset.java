package com.alexandervanderzalm.game.Model;

import com.alexandervanderzalm.game.Utility.IMethodScheduler;
import com.alexandervanderzalm.game.Utility.IProcedureCollection;
import com.alexandervanderzalm.game.Utility.ProcedureCollection;

import java.util.ArrayList;
import java.util.List;

interface IRuleSet<T extends IGame>{
    void SetupGame(T Game);
    // Turn Structure?
}

interface IKalahaGame extends IGame{
    IPitCollection<IKalahaPit> Pits();
    IKalahaActions Actions();
    ITurn CurrentTurn(); // get/set? how to store
}

interface IKalahaActions{
    IProcedureCollection ExtraTurn();//can this be a procedure instead?
    IProcedureCollection Capture();
    IProcedureCollection EndGame();
}

interface ITurn{
    // Functionality
    IMethodScheduler<ITurn> EndOfTurn();


    // Actual Data Object
    //TurnData Data();

    // Shortcuts -- Maybe move to Data?
    //IKalahaActions Actions();
    ITransformCollection Transforms();
    Integer Player();
    IPitCollection<IKalahaPit> pits(); // Should this be in IKalahaTurn? -> maybe move to data?
}

public class KalahaRuleset<T extends IKalahaGame> implements IRuleSet<T> {

    @Override
    public void SetupGame(IKalahaGame Game) {
        // Setup actions
        Game.Actions().ExtraTurn().Add(() -> {
            Game.CurrentTurn().EndOfTurn().ScheduleMethod((turn) -> {
                if (turn.Player() == 0)
                    TurnUtil.SetGameState(turn, GameState.TurnP1);
                else //if(turn.Player() == 1)
                    TurnUtil.SetGameState(turn, GameState.TurnP2);
            });
        });

        // Setup board
        List<IKalahaPit> pits = new ArrayList<>();

        for(int player = 0; player<2; player++) {
            IKalahaPit kalaha = new KalahaPit(new ProcedureCollection());
            kalaha.MakeKalaha();
            kalaha.SetPlayer(player);
            // ### RULE
            // Whenever a kalaha pit gets changed. Schedule a method for the end of the turn that checks
            // if it is the last pit happens to be the players own kalaha. If so that player gets another
            // turn.
            kalaha.OnChanged().Add(
                () -> {
                if(OwnKalaha(kalaha)){
                    Game.CurrentTurn().EndOfTurn().ScheduleMethod(
                            (turn) ->  ExtraTurn(turn)
                    );
                }
            });

            // Add to pit collection
            pits.add(kalaha);

            // TODO - Observed collection creation
            // Create all the normal pits
            for(int normalPitIndex = 0; normalPitIndex < 6; normalPitIndex++) {
                // Prepare a normal pit
                IKalahaPit normalPit = new KalahaPit(new ProcedureCollection());
                normalPit.SetPlayer(player);
                // ### RULE
                // When the changed pit is the players own and empty, then schedule
                // a capture action (move the stones of the opposite pit to this pit).
                // TODO check if it is actually the players turn... (I think thats already there)
                normalPit.OnChanged().Add(
                    () -> {
                        if(OwnEmpty(normalPit, Game.CurrentTurn().Player())){
                            Game.CurrentTurn().EndOfTurn().ScheduleMethod(
                                (turn) -> Capture(normalPit, turn)
                            );
                        }
                    }
                );

                //TODO - Add to observed collection
                pits.add(normalPit);
            }
        }

        // Setup actions
        //Game.Actions().ExtraTurn().Add((turn) -> );

        // TODO add all pits to the games/turns pit collection
        //Game.Pits().

    }

    // Turn structure
    // Do a streams analysis on transforms.filter(PitTransform p -> p.Amount == 0)
    // If so game is over

    private void Capture(IKalahaPit normalPit, ITurn turn){//IPitCollection pits) {
        turn.pits().Opposite(normalPit);
        // TODO capture logic
        turn.Transforms().Transforms().add(new NormalTransform("Capture"));
    }

    private boolean OwnEmpty(IKalahaPit normalPit, int player) {
        return normalPit.Amount() == 0 && normalPit.GetPlayer() == player;
    }

    // Essentially wrappers?
    // Move to util?
    private Boolean OwnKalaha(IKalahaPit p) {
        return p.IsKalaha();
    }

    private void ExtraTurn(ITurn turn){
        // TODO extra turn logic
        turn.Transforms().Transforms().add(new NormalTransform("Extra turn"));
    }

    // TODO
    // Somehow implement all the logic inside here

    //Setup
    /*
    // Board
    2*{
        //1*Board.Pits.Add().OnLast(Turn turn)->turn.OnTurnComplete.Add(ExtraTurn())
        //6*Board.Pits.Add(6).OnLast(Turn turn)->{if(Empty())
        2*1*Board.Pits.Add()  <- Pit.OnChanged.Add(turn.EndOfTurn.Schedule((turn) -> { if(OwnKalaha(LastPit(turn))) ExtraTurn(turn)});
        2*6*Boards.Pit.Add()  <- Pit.OnChanged.Add(turn.EndOfTurn.Schedule((turn) -> { if(OwnEmpty (CurrentPit(turn))) Capture(Opposite(CurrentPit(turn)});
                              <- PitCollection.OnDirtySchedulars.Add(turn.EndOfTurn);
                              <- PitCollection.OnDirty.Add(turn.EndOfTurn, ###ENDOFGAME? -> ENDGAME/HARVEST

        // TODO
        // V - ObserveChange
        // V - EventScheduler<T> (optional priority sorting?)
        // X - ObserveChangeCollection
        // X - TurnUtil   - CurrentPit,OppositePit (turn) -> use PitCollection & transformdata
        // X - PitUtil    - OwnEmpty, OwnKalaha (pit)
        // X - ActionsService/Util
                            <- PitCollection.OnCha
    }
     */

    // Turn - Turn.Functions & Turn.Data
    /*
    OnTurnStart.Add(() -> Turn.Counter++);
    OnTurn.Add(() -> PickUpAllAndDropOneToTheRightTillEmpty(turn.Pits,int index)
    OnTurn.Add -> Turn.Board@last.OnLast() ==== POTENTIAL SOLUTION
     */
}