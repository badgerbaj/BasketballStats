package com.toddbray.basketballstats;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import java.security.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Brad on 4/24/2017.
 */

public class MyDbDataSource extends AsyncTask<Context, Integer, String> {

    private static final String MYSQL_HOST = "jdbc:mysql://foxi.wuffhost.ovh:3306/t_bray_bball_stats";
    private static final String MYSQL_USERNAME = "t_bray_19751087";
    private static final String MYSQL_PASSWORD = "bballadmin16";

    private List<GameModel> games;
    private List<PlayerModel> players;
    private List<StatModel> stats;
    private List<SeasonModel> seasons;

    private String insertQuery, updateQuery;

    @Override
    protected String doInBackground(Context... contexts) {
        try {
            // Open SQLite connection
            DbDataSource sqLite = new DbDataSource(contexts[0]);
            sqLite.open();

            notifyUser(contexts[0], "Synchronizing, Please Wait...");

            // Open MySQL connection
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(MYSQL_HOST, MYSQL_USERNAME, MYSQL_PASSWORD);

            Statement st = con.createStatement();

            ///////////////////////// PLAYER DATA //////////////////////////////////////////////////

            // Send all player data
            players = sqLite.getAllPlayers();
            if(players.size() > 0) {
                for (PlayerModel player : players) {
                    createPlayerQuery(player, false);
                    st.executeUpdate(insertQuery);
                    st.executeUpdate(updateQuery);

                    if (isCancelled()) {
                        notifyUser(contexts[0], "Synchronization Cancelled.");
                        return null;
                    }

                    Thread.sleep(30);
                }
            }
            // Get all MySQL player data
            players = getAllPlayers(st);
            if (players.size() > 0) {
                for (PlayerModel player : players) {
                    createPlayerQuery(player, true);
                    sqLite.runQuery(insertQuery);
                    sqLite.runQuery(updateQuery);

                    if (isCancelled()) {
                        notifyUser(contexts[0], "Synchronization Cancelled.");
                        return null;
                    }
                    Thread.sleep(30);
                }
            }
            ///////////////////////// END PLAYER DATA //////////////////////////////////////////////

            ///////////////////////// SEASON DATA //////////////////////////////////////////////////

            // Send all season data
            seasons = sqLite.getAllSeasons();
            if(seasons.size() > 0) {
                for (SeasonModel season : seasons) {
                    createSeasonQuery(season, false);
                    st.executeUpdate(insertQuery);
                    st.executeUpdate(updateQuery);

                    if (isCancelled()) {
                        notifyUser(contexts[0], "Synchronization Cancelled.");
                        return null;
                    }
                    Thread.sleep(30);
                }
            }
            // Get all MySQL season data
            seasons = getAllSeasons(st);
            if(seasons.size() > 0) {
                for (SeasonModel season : seasons) {
                    createSeasonQuery(season, true);
                    sqLite.runQuery(insertQuery);
                    sqLite.runQuery(updateQuery);

                    if (isCancelled()) {
                        notifyUser(contexts[0], "Synchronization Cancelled.");
                        return null;
                    }
                    Thread.sleep(30);
                }
            }
            ///////////////////////// END SEASON DATA //////////////////////////////////////////////

            ///////////////////////// GAME DATA ////////////////////////////////////////////////////

            // Send all game data
            games = sqLite.getAllGames();
            if(games.size() > 0) {
                for (GameModel game : games) {
                    createGameQuery(game, false);
                    st.executeUpdate(insertQuery);
                    st.executeUpdate(updateQuery);

                    if (isCancelled()) {
                        notifyUser(contexts[0], "Synchronization Cancelled.");
                        return null;
                    }
                    Thread.sleep(30);
                }
            }
            // Receive all MySQL game data
            games = getAllGames(st);
            if(games.size() > 0) {
                for (GameModel game : games) {
                    createGameQuery(game, true);
                    sqLite.runQuery(insertQuery);
                    sqLite.runQuery(updateQuery);

                    if (isCancelled()) {
                        notifyUser(contexts[0], "Synchronization Cancelled.");
                        return null;
                    }
                    Thread.sleep(30);
                }
            }
            ///////////////////////// END GAME DATA ////////////////////////////////////////////////

            ///////////////////////// STAT DATA ////////////////////////////////////////////////////

            // Send all stat data
            stats = sqLite.getAllStats();
            if (stats.size() > 0) {
                for (StatModel stat : stats) {
                    createStatQuery(stat, false);
                    st.executeUpdate(insertQuery);
                    st.executeUpdate(updateQuery);

                    if (isCancelled()) {
                        notifyUser(contexts[0], "Synchronization Cancelled.");
                        return null;
                    }
                    Thread.sleep(30);
                }
            }
            // Get all MySQL stat data
            stats = getAllStats(st);
            if(stats.size() > 0) {
                for (StatModel stat : stats) {
                    createStatQuery(stat, true);
                    sqLite.runQuery(insertQuery);
                    sqLite.runQuery(updateQuery);

                    if (isCancelled()) {
                        notifyUser(contexts[0], "Synchronization Cancelled.");
                        return null;
                    }
                    Thread.sleep(30);
                }
            }
            ///////////////////////// END STAT DATA ////////////////////////////////////////////////

            notifyUser(contexts[0], "Synchronization Complete!");

            con.close();
            sqLite.close();
            return null;
        }
        catch(Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    private void createGameQuery(GameModel gameModel, boolean isSQLite) {

        java.sql.Timestamp game_date;
        java.sql.Timestamp girls_jv;
        java.sql.Timestamp boys_jv;
        java.sql.Timestamp girls_v;
        java.sql.Timestamp boys_v;

        if(!isSQLite) {
            game_date = new java.sql.Timestamp(gameModel.getGame_date().getTime());
            girls_jv = new java.sql.Timestamp(gameModel.getGirls_jv().getTime());
            boys_jv = new java.sql.Timestamp(gameModel.getBoys_jv().getTime());
            girls_v = new java.sql.Timestamp(gameModel.getGirls_v().getTime());
            boys_v = new java.sql.Timestamp(gameModel.getBoys_v().getTime());
        }
        else {
            Date date = new Date();
            game_date = new java.sql.Timestamp(date.getTime());
            girls_jv = new java.sql.Timestamp(date.getTime());
            boys_jv = new java.sql.Timestamp(date.getTime());
            girls_v = new java.sql.Timestamp(date.getTime());
            boys_v = new java.sql.Timestamp(date.getTime());
        }

        insertQuery = "INSERT ";
        if(isSQLite) insertQuery += "OR";
        insertQuery += " IGNORE INTO " + MySqlLiteHelper.GAME_TABLE +
                " VALUES ( '"+ gameModel.getAndroid_id().toString() + "' , " +
                gameModel.getGame_id() + " , " +
                gameModel.getSeason_id() + " , ";
        if(isSQLite) insertQuery += "'" + gameModel.getGame_date().toString() + "' , ";
        else insertQuery += "'" + game_date + "' , ";
        insertQuery += "'" + gameModel.getLocation() + "' , " +
            "'" + gameModel.getVenue() + "' , ";
        if(isSQLite) insertQuery +=
            "'" + gameModel.getGirls_jv().toString() + "' , " +
            "'" + gameModel.getBoys_jv().toString() + "' , " +
            "'" + gameModel.getGirls_v().toString() + "' , " +
            "'" + gameModel.getBoys_v().toString() + "' , ";
        else insertQuery +=
            "'" + girls_jv + "' , " +
            "'" + boys_jv + "' , " +
            "'" + girls_v + "' , " +
            "'" + boys_v + "' , ";
        insertQuery +=
            "'" + gameModel.getOpp_name() + "'" +
            " )";

        updateQuery =
            "UPDATE " + MySqlLiteHelper.GAME_TABLE + " " +
            "SET " + MySqlLiteHelper.GameColumns.season_id.toString() + " = " + gameModel.getSeason_id() + " , " +
            MySqlLiteHelper.GameColumns.location.toString() + " = '" + gameModel.getLocation() + "' , " +
            MySqlLiteHelper.GameColumns.venue.toString() + " = '" + gameModel.getVenue() + "' , ";
        if (isSQLite) {
            updateQuery +=
                MySqlLiteHelper.GameColumns.game_date.toString() + " = '" + gameModel.getGame_date().toString() + "' , " +
                MySqlLiteHelper.GameColumns.girls_jv.toString() + " = '" + gameModel.getGirls_jv().toString() + "' , " +
                MySqlLiteHelper.GameColumns.boys_jv.toString() + " = '" + gameModel.getBoys_jv().toString() + "' , " +
                MySqlLiteHelper.GameColumns.girls_v.toString() + " = '" + gameModel.getGirls_v().toString() + "' , " +
                MySqlLiteHelper.GameColumns.boys_v.toString() + " = '" + gameModel.getBoys_v().toString() + "' , ";
        }
        else {
            updateQuery +=
            MySqlLiteHelper.GameColumns.game_date.toString() + " = '" + game_date + "' , " +
            MySqlLiteHelper.GameColumns.girls_jv.toString() + " = '" + girls_jv + "' , " +
            MySqlLiteHelper.GameColumns.boys_jv.toString() + " = '" + boys_jv + "' , " +
            MySqlLiteHelper.GameColumns.girls_v.toString() + " = '" + girls_v + "' , " +
            MySqlLiteHelper.GameColumns.boys_v.toString() + " = '" + boys_v + "' , ";
        }
        updateQuery +=
                MySqlLiteHelper.GameColumns.opp_name.toString() + " = '" + gameModel.getOpp_name() + "' " +
                "WHERE " + MySqlLiteHelper.GameColumns.android_id.toString() + " = '" + gameModel.getAndroid_id().toString() + "'"+
                " AND " + MySqlLiteHelper.GameColumns.game_id.toString() + " = " + gameModel.getGame_id();
    }

    private List<GameModel> getAllGames(Statement st) throws SQLException {
        List<GameModel> games = new ArrayList<>();

        ResultSet rs = st.executeQuery("SELECT * FROM " + MySqlLiteHelper.GAME_TABLE);

        rs.first();
        while(!rs.isAfterLast()) {

            games.add(resultSetToGameModel(rs));
            rs.next();
        }
        rs.close();

        return games;
    }

    private GameModel resultSetToGameModel(ResultSet rs) throws SQLException {
        GameModel gameModel = new GameModel(null);
        java.sql.Timestamp timestamp;

        gameModel.setAndroid_id(rs.getString(MySqlLiteHelper.GameColumns.android_id.toString()));
        gameModel.setGame_id(rs.getInt(MySqlLiteHelper.GameColumns.game_id.toString()));
        gameModel.setSeason_id(rs.getInt(MySqlLiteHelper.GameColumns.season_id.toString()));

        timestamp = rs.getTimestamp(MySqlLiteHelper.GameColumns.game_date.toString());
        if (timestamp != null)
            gameModel.setGame_date(new java.util.Date(timestamp.getTime()));
        gameModel.setLocation(rs.getString(MySqlLiteHelper.GameColumns.location.toString()));
        gameModel.setVenue(rs.getString(MySqlLiteHelper.GameColumns.venue.toString()));

        timestamp = rs.getTimestamp(MySqlLiteHelper.GameColumns.girls_jv.toString());
        if (timestamp != null)
            gameModel.setGirls_jv(new java.util.Date(timestamp.getTime()));

        timestamp = rs.getTimestamp(MySqlLiteHelper.GameColumns.boys_jv.toString());
        if (timestamp != null)
            gameModel.setBoys_jv(new java.util.Date(timestamp.getTime()));

        timestamp = rs.getTimestamp(MySqlLiteHelper.GameColumns.girls_v.toString());
        if (timestamp != null)
            gameModel.setGirls_v(new java.util.Date(timestamp.getTime()));

        timestamp = rs.getTimestamp(MySqlLiteHelper.GameColumns.boys_v.toString());
        if (timestamp != null)
            gameModel.setBoys_v(new java.util.Date(timestamp.getTime()));
        gameModel.setOpp_name(rs.getString(MySqlLiteHelper.GameColumns.opp_name.toString()));

        return gameModel;
    }

    private void createPlayerQuery(PlayerModel playerModel, boolean isSQLite) {

        insertQuery = "INSERT ";
        if(isSQLite) insertQuery += "OR";
        insertQuery +=
            " IGNORE INTO " + MySqlLiteHelper.PLAYER_TABLE +
            " VALUES ( '"+ playerModel.getAndroid_id() + "' , " +
            playerModel.getPlayer_id() + " , " +
            "'" + playerModel.getFirst_name() + "' , " +
            "'" + playerModel.getLast_name() + "' , " +
            "'" + playerModel.getYear() + "' , " +
            playerModel.getNumber() +
            " )";

        updateQuery =
            "UPDATE " + MySqlLiteHelper.PLAYER_TABLE + " " +
            "SET " + MySqlLiteHelper.PlayerColumns.first_name.toString() + " = '" + playerModel.getFirst_name() + "' , " +
            MySqlLiteHelper.PlayerColumns.last_name.toString() + " = '" + playerModel.getLast_name() + "' , " +
            MySqlLiteHelper.PlayerColumns.year.toString() + " = '" + playerModel.getYear() + "' , " +
            MySqlLiteHelper.PlayerColumns.number.toString() + " = '" + playerModel.getNumber() + "' " +
            "WHERE " + MySqlLiteHelper.PlayerColumns.android_id.toString() + " = '" + playerModel.getAndroid_id().toString() + "'" +
            " AND " + MySqlLiteHelper.PlayerColumns.player_id.toString() + " = " + playerModel.getPlayer_id();
    }

    private List<PlayerModel> getAllPlayers(Statement st) throws SQLException {
        List<PlayerModel> players = new ArrayList<>();

        ResultSet rs = st.executeQuery("SELECT * FROM " + MySqlLiteHelper.PLAYER_TABLE);

        rs.first();
        while(!rs.isAfterLast()) {

            players.add(resultSetToPlayerModel(rs));
            rs.next();
        }
        rs.close();

        return players;
    }

    private PlayerModel resultSetToPlayerModel(ResultSet rs) throws SQLException {
        PlayerModel playerModel = new PlayerModel(null);

        playerModel.setAndroid_id(rs.getString(MySqlLiteHelper.PlayerColumns.android_id.toString()));
        playerModel.setPlayer_id(rs.getInt(MySqlLiteHelper.PlayerColumns.player_id.toString()));
        playerModel.setFirst_name(rs.getString(MySqlLiteHelper.PlayerColumns.first_name.toString()));
        playerModel.setLast_name(rs.getString(MySqlLiteHelper.PlayerColumns.last_name.toString()));
        playerModel.setYear(rs.getString(MySqlLiteHelper.PlayerColumns.year.toString()));
        playerModel.setNumber(rs.getInt(MySqlLiteHelper.PlayerColumns.number.toString()));

        return playerModel;
    }

    private void createStatQuery(StatModel statModel, boolean isSQLite) {

        insertQuery = "INSERT ";
        if(isSQLite) insertQuery += "OR";
        insertQuery +=
            " IGNORE INTO " + MySqlLiteHelper.STAT_TABLE +
            " VALUES ( '"+ statModel.getAndroid_id().toString() + "' , " +
            statModel.getStat_id() + " , " +
            statModel.getGame_id() + " , " +
            statModel.getPlayer_id() + " , " +
            statModel.getO_rebound() + " , " +
            statModel.getD_rebound() + " , " +
            statModel.getAssist() + " , " +
            statModel.getSteal() + " , " +
            statModel.getTurnover() + " , " +
            statModel.getTwo_pointer() + " , " +
            statModel.getTwo_pointer_made() + " , " +
            statModel.getThree_pointer() + " , " +
            statModel.getThree_pointer_made() + " , " +
            statModel.getFree_throw() + " , " +
            statModel.getFree_throw_made() + " , " +
            statModel.getCharge() +
            " )";

        updateQuery =
            "UPDATE " + MySqlLiteHelper.STAT_TABLE + " " +
            "SET " + MySqlLiteHelper.StatColumns.game_id.toString() + " = " + statModel.getGame_id() + " , " +
            MySqlLiteHelper.StatColumns.player_id.toString() + " = " + statModel.getPlayer_id() + " , " +
            MySqlLiteHelper.StatColumns.o_rebound.toString() + " = " + statModel.getO_rebound() + " , " +
            MySqlLiteHelper.StatColumns.d_rebound.toString() + " = " + statModel.getD_rebound() + " , " +
            MySqlLiteHelper.StatColumns.assist.toString() + " = " + statModel.getAssist() + " , " +
            MySqlLiteHelper.StatColumns.steal.toString() + " = " + statModel.getSteal() + " , " +
            MySqlLiteHelper.StatColumns.turnover.toString() + " = " + statModel.getTurnover() + " , " +
            MySqlLiteHelper.StatColumns.two_pointer.toString() + " = " + statModel.getTwo_pointer() + " , " +
            MySqlLiteHelper.StatColumns.two_pointer_made.toString() + " = " + statModel.getTwo_pointer_made() + " , " +
            MySqlLiteHelper.StatColumns.three_pointer.toString() + " = " + statModel.getThree_pointer() + " , " +
            MySqlLiteHelper.StatColumns.three_pointer_made.toString() + " = " + statModel.getThree_pointer_made() + " , " +
            MySqlLiteHelper.StatColumns.free_throw.toString() + " = " + statModel.getFree_throw() + " , " +
            MySqlLiteHelper.StatColumns.free_throw_made.toString() + " = " + statModel.getFree_throw_made() + " , " +
            MySqlLiteHelper.StatColumns.charge.toString() + " = " + statModel.getCharge() + " " +
            "WHERE " + MySqlLiteHelper.StatColumns.android_id.toString() + " = '" + statModel.getAndroid_id().toString() + "'" +
            " AND " + MySqlLiteHelper.StatColumns.stat_id.toString() + " = " + statModel.getStat_id();
    }

    private List<StatModel> getAllStats(Statement st) throws SQLException {
        List<StatModel> stats = new ArrayList<>();

        ResultSet rs = st.executeQuery("SELECT * FROM " + MySqlLiteHelper.STAT_TABLE );

        rs.first();
        while(!rs.isAfterLast()) {

            stats.add(resultSetToStatModel(rs));
            rs.next();
        }
        rs.close();

        return stats;
    }

    private StatModel resultSetToStatModel(ResultSet rs) throws SQLException {
        StatModel statModel = new StatModel(null);

        statModel.setAndroid_id(rs.getString(MySqlLiteHelper.StatColumns.android_id.toString()));
        statModel.setStat_id(rs.getInt(MySqlLiteHelper.StatColumns.stat_id.toString()));
        statModel.setGame_id(rs.getInt(MySqlLiteHelper.StatColumns.game_id.toString()));
        statModel.setPlayer_id(rs.getInt(MySqlLiteHelper.StatColumns.player_id.toString()));
        statModel.setO_rebound(rs.getInt(MySqlLiteHelper.StatColumns.o_rebound.toString()));
        statModel.setD_rebound(rs.getInt(MySqlLiteHelper.StatColumns.d_rebound.toString()));
        statModel.setAssist(rs.getInt(MySqlLiteHelper.StatColumns.assist.toString()));
        statModel.setSteal(rs.getInt(MySqlLiteHelper.StatColumns.steal.toString()));
        statModel.setTurnover(rs.getInt(MySqlLiteHelper.StatColumns.turnover.toString()));
        statModel.setTwo_pointer(rs.getInt(MySqlLiteHelper.StatColumns.two_pointer.toString()));
        statModel.setTwo_pointer_made(rs.getInt(MySqlLiteHelper.StatColumns.two_pointer_made.toString()));
        statModel.setThree_pointer(rs.getInt(MySqlLiteHelper.StatColumns.three_pointer.toString()));
        statModel.setThree_pointer_made(rs.getInt(MySqlLiteHelper.StatColumns.three_pointer_made.toString()));
        statModel.setFree_throw(rs.getInt(MySqlLiteHelper.StatColumns.free_throw.toString()));
        statModel.setFree_throw_made(rs.getInt(MySqlLiteHelper.StatColumns.free_throw_made.toString()));
        statModel.setCharge(rs.getInt(MySqlLiteHelper.StatColumns.charge.toString()));

        return statModel;
    }

    private void createSeasonQuery(SeasonModel seasonModel, boolean isSQLite) {

        insertQuery = "INSERT ";
        if(isSQLite) insertQuery += "OR";
        insertQuery +=
            " IGNORE INTO " + MySqlLiteHelper.SEASON_TABLE +
            " VALUES ( '"+ seasonModel.getAndroid_id().toString() + "' , " +
            seasonModel.getSeason_id() + " , " +
            seasonModel.getSeason_name() +
            " )";

        updateQuery =
            "UPDATE " + MySqlLiteHelper.SEASON_TABLE + " " +
            "SET " + MySqlLiteHelper.SeasonColumns.season_name.toString() + " = " + seasonModel.getSeason_name() + " " +
            "WHERE " + MySqlLiteHelper.SeasonColumns.android_id.toString() + " = '" + seasonModel.getAndroid_id().toString() + "'" +
            " AND " + MySqlLiteHelper.SeasonColumns.season_id.toString() + " = " + seasonModel.getSeason_id();
    }

    private List<SeasonModel> getAllSeasons(Statement st) throws SQLException {
        List<SeasonModel> seasons = new ArrayList<>();

        ResultSet rs = st.executeQuery("SELECT * FROM " + MySqlLiteHelper.SEASON_TABLE);

        rs.first();
        while(!rs.isAfterLast()) {

            seasons.add(resultSetToSeasonModel(rs));
            rs.next();
        }
        rs.close();

        return seasons;
    }

    private SeasonModel resultSetToSeasonModel(ResultSet rs) throws SQLException {
        SeasonModel seasonModel = new SeasonModel(null);

        seasonModel.setAndroid_id(rs.getString(MySqlLiteHelper.SeasonColumns.android_id.toString()));
        seasonModel.setSeason_id(rs.getInt(MySqlLiteHelper.SeasonColumns.season_id.toString()));
        seasonModel.setSeason_name(rs.getInt(MySqlLiteHelper.SeasonColumns.season_name.toString()));

        return seasonModel;
    }

    private void notifyUser (Context ctx, String message) {
        // Notify User
        final Context c = ctx;
        final String s = message;

        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                Toast.makeText(c, s, Toast.LENGTH_LONG).show();
            }
        });
    }
}
