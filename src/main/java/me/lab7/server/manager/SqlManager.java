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
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS Coordinates(" +
            "    coordinates_id BIGSERIAL PRIMARY KEY," +
            "X_coordinates  REAL," +
            "    Y_coordinates  BIGINT NOT NULL" +
            ");" +
            "CREATE TABLE IF NOT EXISTS Discipline(" +
            "    discipline_id   BIGSERIAL PRIMARY KEY," +
            "    discipline_name VARCHAR(127)," +
            "    practice_hours  INTEGER" +
            ");" +
            "CREATE TABLE IF NOT EXISTS Labworks(" +
            "    id            BIGSERIAL PRIMARY KEY," +
            "    name          VARCHAR(127) NOT NULL," +
            "    coordinates   BIGINT REFERENCES Coordinates (coordinates_id) ON DELETE CASCADE," +
            "    creation_date DATE NOT NULL," +
            "    minimal_point  BIGINT NOT NULL," +
            "    difficulty    VARCHAR(9)," +
            "    discipline    BIGINT REFERENCES Discipline (discipline_id) ON DELETE CASCADE," +
            "    owner_id      INTEGER NOT NULL REFERENCES users(id) on DELETE CASCADE" +
            ");";

    public SqlManager(Connection conn, Logger logger) {
        this.conn = conn;
        this.logger = logger;
    }

    public void initTableOrExecuteLabWorks() throws SQLException {
        try (Statement s = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            s.execute(CREATE_TABLE);
            ResultSet res = s.executeQuery("SELECT *" +
                    "FROM Labworks" +
                    "         INNER JOIN Coordinates ON Labworks.coordinates = Coordinates.coordinates_id" +
                    "         INNER JOIN Discipline ON Labworks.discipline = Discipline.discipline_id;");
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
            labWork.setCreationDate(res.getDate("creation_date").toLocalDate());
            labWork.setMinimalPoint(res.getLong("minimal_point"));

            if (res.getString("difficulty") != null) {
                labWork.setDifficulty(res.getString("difficulty"));
            }
            if (res.getString("discipline_name") != null) {
                labWork.getDiscipline().setName(res.getString("discipline_name"));
                labWork.getDiscipline().setPracticeHours(res.getInt("practice_hours"));
                labWork.setOwnerID(res.getLong("owner_id"));
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
