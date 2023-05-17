package me.lab7.server.manager;

import me.lab7.common.data.LabWork;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Logger;

public class SqlManager {
    private final Connection conn;
    private final Logger logger;
    private NavigableSet<LabWork> labWorks = new TreeSet<>();
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS LabWorks("
            + "   id BIGSERIAL PRIMARY KEY,"
            + "   name varchar(127) NOT NULL,"
            + "   X_coordinates real,"
            + "   Y_coordinates BIGINT NOT NULL,"
            + "   creation_date DATE NOT NULL,"
            + "   minimalpoint BIGINT NOT NULL,"
            + "   difficult varchar(9) ,"
            + "   discipline_name varchar(127) ,"
            + "   practice_hours integer,"
            + "   owner_id integer NOT NULL,"
            + "   CONSTRAINT fk_owner"
            + "      FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE)";

    public SqlManager(Connection conn, Logger logger) {
        this.conn = conn;
        this.logger = logger;
    }

    public void initTableOrExecuteLabWorks() throws SQLException {
        try (Statement s = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            s.execute(CREATE_TABLE);
            ResultSet res = s.executeQuery("SELECT * FROM LabWork");
            int invalidRoutes = 0;
            while (res.next()) {
                LabWork labWork = addToCollection(res);
                if (labWork != null) {
                    labWorks.add(labWork);
                } else {
                    invalidRoutes++;
                }
            }

            logger.info("Загружено " + labWorks.size() + " лабораторных из базы дынных, не удалось загрузить " + invalidRoutes + " лабораторных.");

        }
    }

    public LabWork addToCollection(ResultSet res) {
        try {
            LabWork labWork = new LabWork();

            labWork.setId(res.getLong("id"));
            labWork.setName(res.getString("name"));
            labWork.getCoordinates().setX(res.getFloat("X_coordinates"));
            labWork.getCoordinates().setY(res.getLong("Y_coordinates"));
            labWork.setCrationDate(res.getDate("creation_date").toLocalDate());
            labWork.setMinimalPoint(res.getLong("minimal_point"));
            if (!res.getString("difficulty").equals("")) labWork.setDifficulty(res.getString("difficulty"));
            if (!res.getString("discipline_name").equals("")) {
                labWork.getDiscipline().setName(res.getString("discipline_name"));
                labWork.getDiscipline().setPracticeHours(res.getInt("practice_hours"));
            } else {
                labWork.setDiscipline(null);
            }
            return labWork;
        } catch (Exception ex) {
            try {
                logger.warning("при добавлении лабораторной с id=" + res.getLong("id") + "произошла ошибка, лабораторная не была добавлена в коллекцию");
            } catch (SQLException e) {
                logger.warning("при добавлении лабораторной в коллекцию произошла ошибка, невозможно определить какая лабораторная не была добавлена");
                return null;
            }
            return null;
        }
    }

    public NavigableSet<LabWork> getCollection() {
        return labWorks;
    }
}
