package me.lab7.server.manager;

import me.lab7.common.data.LabWork;

import java.sql.*;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Logger;

public class SqlCollectionManager {
    private final Connection conn;
    private final Logger logger;
    private final NavigableSet<LabWork> labWorks = new TreeSet<>();
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS Coordinates(" +
            "    coordinates_id BIGSERIAL PRIMARY KEY," +
            "X_coordinates  REAL," +
            "    Y_coordinates  BIGINT NOT NULL " +
            ");" +
            "CREATE TABLE IF NOT EXISTS Discipline(" +
            "    discipline_id   BIGSERIAL PRIMARY KEY," +
            "    discipline_name VARCHAR(127)," +
            "    practice_hours  INTEGER" +
            ");" +
            "CREATE TABLE IF NOT EXISTS Labworks(" +
            "    id            BIGSERIAL PRIMARY KEY," +
            "    name          VARCHAR(127) NOT NULL," +
            "    coordinates   BIGINT NOT NULL REFERENCES Coordinates(coordinates_id) ON DELETE CASCADE," +
            "    creation_date DATE NOT NULL," +
            "    minimal_point  BIGINT NOT NULL," +
            "    difficulty    VARCHAR(9)," +
            "    discipline    BIGINT REFERENCES Discipline(discipline_id) ON DELETE SET NULL," +
            "    owner_id      INTEGER NOT NULL REFERENCES users(id) on DELETE CASCADE" +
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
                    "         LEFT JOIN Coordinates ON Labworks.coordinates = Coordinates.coordinates_id" +
                    "         LEFT JOIN Discipline ON Labworks.discipline = Discipline.discipline_id;");
            while (res.next()) {
                LabWork labWork = ReadDBAndSaveInCollection(res);
                labWorks.add(labWork);
            }

            logger.info("Загружено " + labWorks.size() + " лабораторных из базы данных.");

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
                logger.warning("при добавлении лабораторной в коллекцию произошла ошибка, невозможно определить какая лабораторная не была добавлена " + e);
                return null;
            }
            return null;
        }
    }

    public Long addInDB(LabWork labWork, Long userId) throws Exception {
        String DISCIPLINE = "INSERT INTO Discipline (discipline_name, practice_hours)" +
                "VALUES (?, ?) RETURNING discipline_id;";
        String COORDINATES = "INSERT INTO Coordinates (x_coordinates, y_coordinates)" +
                "VALUES (?, ?) RETURNING coordinates_id;";
        String LABWORK = "INSERT INTO Labworks (name,coordinates,  creation_date, minimal_point,  difficulty,discipline, owner_id)" +
                "VALUES (?, ?, ?, ?, ?,?,?) RETURNING id;";
        Long coordinatesID;
        try (PreparedStatement preparedStatement = conn.prepareStatement(COORDINATES)) {
            preparedStatement.setFloat(1, labWork.getCoordinates().getX());
            preparedStatement.setLong(2, labWork.getCoordinates().getY());
            ResultSet coordinate = preparedStatement.executeQuery();
            coordinate.next();
            coordinatesID = coordinate.getLong(1);
        }
        Long disciplineID = null;
        if (labWork.getDiscipline() != null) {
            try (PreparedStatement preparedStatement = conn.prepareStatement(DISCIPLINE)) {
                preparedStatement.setString(1, labWork.getDiscipline().getName());
                preparedStatement.setInt(2, labWork.getDiscipline().getPracticeHours());
                ResultSet discipline = preparedStatement.executeQuery();
                discipline.next();
                disciplineID = discipline.getLong(1);
            }
        }
        String difficulty = null;
        if (labWork.getDifficulty() != null) {
            difficulty = labWork.getDifficulty().toString();
        }
        long labWorkID;
        try (PreparedStatement preparedStatement = conn.prepareStatement(LABWORK)) {
            preparedStatement.setString(1, labWork.getName());
            preparedStatement.setLong(2, coordinatesID);
            preparedStatement.setDate(3, Date.valueOf(labWork.getCreationDate()));
            preparedStatement.setLong(4, labWork.getMinimalPoint());
            preparedStatement.setString(5, difficulty);
            if (disciplineID == null) {
                preparedStatement.setNull(6, Types.BIGINT);
            } else {
                preparedStatement.setLong(6, disciplineID);
            }
            preparedStatement.setLong(7, userId);
            ResultSet generatedKeys = preparedStatement.executeQuery();
            generatedKeys.next();
            labWorkID = generatedKeys.getLong(1);
        }

        return labWorkID;
    }

    public NavigableSet<LabWork> getCollection() {
        return labWorks;
    }

    public void clear(Long userId) throws SQLException {
        String DISCIPLINE = "DELETE FROM discipline WHERE discipline_id IN (SELECT id FROM labworks WHERE owner_id=?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(DISCIPLINE)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.executeUpdate();
        }
        String CLEAR_COORDINATES = "DELETE FROM coordinates WHERE coordinates_id IN (SELECT id FROM labworks WHERE owner_id=?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR_COORDINATES)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.executeUpdate();
        }
        String CLEAR = "DELETE FROM labworks WHERE owner_id=?;";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.executeUpdate();
        }
    }

    public void removeByID(Long id, Long userId) throws SQLException {
        String DISCIPLINE = "DELETE FROM discipline WHERE discipline_id IN (SELECT id FROM labworks WHERE id=? and owner_id=?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(DISCIPLINE)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
        String CLEAR_COORDINATES = "DELETE FROM coordinates WHERE coordinates_id IN (SELECT id FROM labworks WHERE id=? and owner_id=?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR_COORDINATES)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
        String CLEAR = "DELETE FROM labworks WHERE id=? and owner_id=? RETURNING coordinates, discipline";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }

    }

    public void removeGreater(Long id, Long userId) throws SQLException {
        String DISCIPLINE = "DELETE FROM discipline WHERE discipline_id IN (SELECT id FROM labworks WHERE id>? and owner_id=?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(DISCIPLINE)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
        String CLEAR_COORDINATES = "DELETE FROM coordinates WHERE coordinates_id IN (SELECT id FROM labworks WHERE id>? and owner_id=?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR_COORDINATES)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
        String CLEAR = "DELETE FROM labworks WHERE id>? and owner_id=? ";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
    }

    public void removeLower(Long id, Long userId) throws SQLException {
        String DISCIPLINE = "DELETE FROM discipline WHERE discipline_id IN (SELECT id FROM labworks WHERE id<? and owner_id=?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(DISCIPLINE)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
        String CLEAR_COORDINATES = "DELETE FROM coordinates WHERE coordinates_id IN (SELECT id FROM labworks WHERE id<? and owner_id=?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR_COORDINATES)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
        String CLEAR = "DELETE FROM labworks WHERE id<? and owner_id=?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(CLEAR)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
    }

    public void update(LabWork labWork, Long userId) throws SQLException {
        String LABWORK = "UPDATE Labworks SET name = ?, creation_date = ?, minimal_point = ?, difficulty = ?,discipline = ?, owner_id = ? WHERE id = ?;";
        String DISCIPLINE = "UPDATE Discipline SET discipline_name = ?, practice_hours =? WHERE discipline_id = (SELECT labworks.discipline FROM labworks where id = ?)";
        String COORDINATES = "UPDATE Coordinates SET x_coordinates = ?, y_coordinates = ? WHERE coordinates_id = (SELECT labworks.coordinates FROM labworks where id = ?);";
        String difficulty = null;
        if (labWork.getDifficulty() != null) {
            difficulty = labWork.getDifficulty().toString();
        }
        var labWorkID = labWork.getId();
        try (PreparedStatement preparedStatement = conn.prepareStatement(LABWORK)) {
            preparedStatement.setString(1, labWork.getName());
            preparedStatement.setDate(2, Date.valueOf(labWork.getCreationDate()));
            preparedStatement.setLong(3, labWork.getMinimalPoint());
            preparedStatement.setString(4, difficulty);
            preparedStatement.setLong(5, userId);
            preparedStatement.setLong(6, labWorkID);
            preparedStatement.executeUpdate();
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
    }
}
