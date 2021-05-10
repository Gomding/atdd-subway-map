package wooteco.subway.station.repository.infra;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import wooteco.subway.exception.DuplicatedNameException;
import wooteco.subway.station.domain.Station;
import wooteco.subway.station.repository.StationRepository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class JDBCStationRepository implements StationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Station> stationRowMapper = (resultSet, rowNum) ->
            new Station(
                    resultSet.getLong("id"),
                    resultSet.getString("name")
            );

    public JDBCStationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Station save(final Station station) {
        try {
            String query = "INSERT INTO station (name) VALUES(?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            this.jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(query, new String[]{"id"});
                ps.setString(1, station.getName());
                return ps;
            }, keyHolder);
            return findById(keyHolder.getKey().longValue()).get();
        } catch (DuplicateKeyException e) {
            throw new DuplicatedNameException("이미 존재하는 지하철 역 이름입니다.", e.getCause());
        }
    }

    @Override
    public Optional<Station> findById(final Long id) {
        String query = "SELECT * FROM station WHERE id = ?";
        return Optional.ofNullable(this.jdbcTemplate.queryForObject(query, stationRowMapper, id));
    }

    @Override
    public Optional<Station> findByName(final String name) {
        String query = "SELECT * FROM station WHERE name = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(query, stationRowMapper, name));
    }

    @Override
    public List<Station> findAll() {
        String query = "SELECT * FROM station";
        return this.jdbcTemplate.query(query, stationRowMapper);
    }

    @Override
    public void delete(final Long id) {
        String query = "DELETE FROM station WHERE id = ?";
        this.jdbcTemplate.update(query, id);
    }

    @Override
    public void deleteAll() {
        String query = "DELETE FROM station";
        this.jdbcTemplate.update(query);
    }
}
