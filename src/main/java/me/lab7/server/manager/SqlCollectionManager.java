package me.lab7.server.manager;

import me.lab7.common.data.LabWork;

import java.sql.*;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Logger;

public class SqlCollectionManager {
    private final Connection conn;
    private final Logger logger;
    private NavigableSet<LabWork> labWorks = new TreeSet<>();
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS Labworks(" +
            "    id            BIGSERIAL PRIMARY KEY," +
            "    name          VARCHAR(127) NOT NULL," +
            "    creation_date DATE NOT NULL," +
            "    minimal_point  BIGINT NOT NULL," +
            "    difficulty    VARCHAR(9)," +
            "    owner_id     BIGINT  NOT NULL REFERENCES users (id) on DELETE CASCADE" +
            ");" +
            "CREATE TABLE IF NOT EXISTS Coordinates(" +
            "    coordinates_id BIGSERIAL PRIMARY KEY," +
            "    X_coordinates  REAL," +
            "    Y_coordinates  BIGINT NOT NULL," +
            "    labwork_id BIGINT NOT NULL REFERENCES Labworks(id) on DELETE CASCADE" +
            ");" +
            "CREATE TABLE IF NOT EXISTS Discipline(" +
            "    discipline_id   BIGSERIAL PRIMARY KEY," +
            "    discipline_name VARCHAR(127)," +
            "    practice_hours  INTEGER," +
            "    labwork_id BIGINT NOT NULL REFERENCES Labworks(id) on DELETE CASCADE" +
            ");";

    public SqlCollectionManager(Connection conn, Logger logger) {
        this.conn = conn;
        this.logger = logger;
    }

    public void initTableOrExecuteLabWorks() throws SQLException {
        try (Statement s = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            s.execute(CREATE_TABLE);
            ResultSet res = s.executeQuery("SELECT *" +
                    "FROM Labworks" +
                    "         JOIN Coordinates ON Labworks.id = Coordinates.labwork_id" +
                    "         LEFT JOIN Discipline ON Labworks.id = Discipline.labwork_id;");
            while (res.next()) {
                LabWork labWork = ReadDBAndSaveInCollection(res);
                labWorks.add(labWork);
            }

            logger.info("Загружено " + labWorks.size() + " лабораторных из базы дынных.");

        }
    }

    public LabWork ReadDBAndSaveInCollection(ResultSet res) {
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
            } else {
                labWork.setDiscipline(null);
            }
            labWork.setOwnerID(res.getLong("owner_id"));
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

    public Long addInDB(LabWork labWork, Long userId) throws Exception {
        String LABWORK = "INSERT INTO Labworks (name, creation_date, minimal_point,  difficulty, owner_id)" +
                "VALUES (?, ?, ?, ?, ?) RETURNING id;";
        String DISCIPLINE = "INSERT INTO Discipline (discipline_name, practice_hours, labwork_id)" +
                "VALUES (?, ?, ?);";
        String COORDINATES = "INSERT INTO Coordinates (x_coordinates, y_coordinates, labwork_id)" +
                "VALUES (?, ?, ?);";
        String difficulty = null;
        if (labWork.getDifficulty() != null) {
            difficulty = labWork.getDifficulty().toString();
        }
        long labWorkID;
        try (PreparedStatement preparedStatement = conn.prepareStatement(LABWORK)) {
            preparedStatement.setString(1, labWork.getName());
            preparedStatement.setDate(2, Date.valueOf(labWork.getCreationDate()));
            preparedStatement.setLong(3, labWork.getMinimalPoint());
            preparedStatement.setString(4, difficulty);
            preparedStatement.setLong(5, userId);
            ResultSet generatedKeys = preparedStatement.executeQuery();
            generatedKeys.next();
            labWorkID = generatedKeys.getLong(1);
        }
        if (labWork.getDiscipline() != null) {
            try (PreparedStatement preparedStatement = conn.prepareStatement(DISCIPLINE)) {
                preparedStatement.setString(1, labWork.getDiscipline().getName());
                preparedStatement.setInt(2, labWork.getDiscipline().getPracticeHours());
                preparedStatement.setLong(3, labWorkID);
                preparedStatement.executeUpdate();
            }
        }
        try (PreparedStatement preparedStatement = conn.prepareStatement(COORDINATES)) {
            preparedStatement.setFloat(1, labWork.getCoordinates().getX());
            preparedStatement.setLong(2, labWork.getCoordinates().getY());
            preparedStatement.setLong(3, labWorkID);
            preparedStatement.executeUpdate();
        }
        return labWorkID;
    }

    public NavigableSet<LabWork> getCollection() {
        return labWorks;
    }

    public void clear(Long client) throws SQLException {
        String CLEAR = "DELETE FROM labworks WHERE owner_id=?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR)) {
            preparedStatement.setLong(1, client);
            preparedStatement.executeUpdate();
        }
    }

    public void removeByID(Long id, Long client) throws SQLException {
        String CLEAR = "DELETE FROM labworks WHERE id=? and owner_id=? ";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, client);
            preparedStatement.executeUpdate();
        }
    }

    public void removeGreater(Long id, Long client) throws SQLException {
        String CLEAR = "DELETE FROM labworks WHERE id>? and owner_id=? ";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, client);
            preparedStatement.executeUpdate();
        }
    }

    public void removeLower(Long id, Long client) throws SQLException {
        String CLEAR = "DELETE FROM labworks WHERE id<? and owner_id=? ";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, client);
            preparedStatement.executeUpdate();
        }
    }
}
