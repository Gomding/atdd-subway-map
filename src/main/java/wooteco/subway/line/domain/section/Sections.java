package wooteco.subway.line.domain.section;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sections {
    private static final Sections EMPTY = new Sections(Collections.EMPTY_LIST);
    private static final int DELETE_LIMIT_SIZE = 1;
    private static final int END_POINT_COUNT = 1;

    private final List<Section> sections;

    public Sections(List<Section> sections) {
        this.sections = sections;
    }

    public static Sections empty() {
        return EMPTY;
    }

    public void add(Section section) {
        validatePossibleToAdd(section);
        if (isAddToEndPoint(section)) {
            sections.add(section);
            return;
        }
        addToBetween(section);
    }

    public void delete(Long stationId) {
        validateDeleteSize();
        validateExistStationId(stationId);
        if (isDeleteToEndPoint(stationId)) {
            deleteEndPoint(stationId);
            return;
        }
        deleteSectionOfStation(stationId);
    }

    private void deleteSectionOfStation(Long stationId) {
        List<Section> findSections = sections.stream()
                .filter(section -> section.hasStationId(stationId))
                .collect(Collectors.toList());

        sections.add(findSections.get(0).mergeWithoutDuplicateStationId(findSections.get(1)));
        findSections.forEach(sections::remove);
    }

    private void deleteEndPoint(Long stationId) {
        Section findSection = sections.stream()
                .filter(section -> section.hasStationId(stationId))
                .findAny().orElseThrow(() -> new IllegalArgumentException("삭제할 역을 가진 구간이 존재하지 않습니다."));
        sections.remove(findSection);
    }

    private boolean isDeleteToEndPoint(Long stationId) {
        int count = (int) sections.stream()
                .filter(section -> section.hasStationId(stationId))
                .count();
        return count == END_POINT_COUNT;
    }

    private void validateExistStationId(Long stationId) {
        if (isExistStationId(stationId)) {
            return;
        }
        throw new IllegalArgumentException("삭제하려는 역을 포함하는 구간이 존재하지 않습니다.");
    }

    private void validateDeleteSize() {
        if (sections.size() <= DELETE_LIMIT_SIZE) {
            throw new IllegalStateException("구간이 하나 이하일 때는 삭제할 수 없습니다.");
        }
    }

    private void addToBetween(Section newSection) {
        Section section = findSectionToConnect(newSection);
        section.validateAddableDistance(newSection);
        sections.remove(section);
        sections.add(newSection);
        addUpStation(section, newSection);
        addDownStation(section, newSection);
    }


    private void addUpStation(Section section, Section newSection) {
        if (section.isSameDownStationId(newSection)) {
            sections.add(new Section(newSection.getLineId(), section.getUpStationId(),
                    newSection.getUpStationId(), section.distanceDifference(newSection)));
        }
    }

    private void addDownStation(Section section, Section newSection) {
        if (section.isSameUpStationId(newSection)) {
            sections.add(new Section(newSection.getLineId(), newSection.getDownStationId(),
                    section.getDownStationId(), section.distanceDifference(newSection)));
        }
    }

    private Section findSectionToConnect(Section newSection) {
        return sections.stream()
                .filter(section -> section.hasUpStationIdOrDownStationId(newSection))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("연결할 역을 찾지 못했습니다."));
    }

    private boolean isAddToEndPoint(Section newSection) {
        boolean existUpStationId = sections.stream()
                .anyMatch(section -> section.isSameUpStationId(newSection));
        boolean existDownStationId = sections.stream()
                .anyMatch(section -> section.isSameDownStationId(newSection));
        return !existUpStationId && !existDownStationId;
    }

    private void validatePossibleToAdd(Section newSection) {
        boolean existUpStation = isExistStationId(newSection.getUpStationId());
        boolean existDownStation = isExistStationId(newSection.getDownStationId());
        validateAlreadyExistSectionOfStation(existUpStation, existDownStation);
        validateNotExistSectionOfStation(existUpStation, existDownStation);
    }

    private boolean isExistStationId(Long upStationId) {
        return this.stationIds().contains(upStationId);
    }

    private void validateNotExistSectionOfStation(boolean existUpStation, boolean existDownStation) {
        if (!existUpStation && !existDownStation) {
            throw new IllegalArgumentException("연결할 수 있는 역이 구간내에 없습니다.");
        }
    }

    private void validateAlreadyExistSectionOfStation(boolean existUpStation, boolean existDownStation) {
        if (existUpStation && existDownStation) {
            throw new IllegalArgumentException("상행역과 하행역이 이미 존재합니다.");
        }
    }

    private List<Long> stationIds() {
        return sections.stream()
                .flatMap(section -> Stream.of(section.getUpStationId(), section.getDownStationId()))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Section> sortSection() {
        Map<Long, Section> upStationIdMap = sections.stream()
                .collect(Collectors.toMap(Section::getUpStationId, section -> section));

        Deque<Section> sectionDeque = new ArrayDeque<>();
        sectionDeque.add(sections.get(0));
        while (upStationIdMap.containsKey(sectionDeque.peekLast().getDownStationId())) {
            sectionDeque.addLast(upStationIdMap.get(sectionDeque.peekLast().getDownStationId()));
        }

        Map<Long, Section> downStationIdMap = sections.stream()
                .collect(Collectors.toMap(Section::getDownStationId, section -> section));
        while (downStationIdMap.containsKey(sectionDeque.peekFirst().getUpStationId())) {
            sectionDeque.addFirst(downStationIdMap.get(sectionDeque.peekFirst().getUpStationId()));
        }
        return new ArrayList<>(sectionDeque);
    }

    public List<Section> toList() {
        return new ArrayList<>(sections);
    }
}
