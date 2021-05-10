package wooteco.subway.station.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.exception.StationNotFoundException;
import wooteco.subway.station.domain.Station;
import wooteco.subway.station.repository.StationRepository;
import wooteco.subway.station.repository.dto.StationDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Transactional
    public StationDto save(final StationDto stationDto) {
        Station station = stationRepository.save(new Station(stationDto.getName()));
        return StationDto.from(station);
    }

    @Transactional(readOnly = true)
    public List<StationDto> findAll() {
        List<Station> stations = stationRepository.findAll();
        return stations.stream()
                .map(StationDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(final Long id) {
        Optional<Station> optionalStation = stationRepository.findById(id);
        if (optionalStation.isPresent()) {
            stationRepository.delete(id);
            return;
        }
        throw new StationNotFoundException("해당 역이 존재하지 않습니다.");
    }
}
