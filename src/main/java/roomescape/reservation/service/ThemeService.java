package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.dto.ThemeDto;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.ReservationTimeAvailabilities;
import roomescape.reservation.model.Theme;
import roomescape.reservation.controller.request.SaveThemeRequest;
import roomescape.reservation.repository.CustomThemeRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Transactional
@Service
public class ThemeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final CustomThemeRepository customThemeRepository;

    public ThemeService(
            final CustomThemeRepository customThemeRepository,
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository
    ) {
        this.customThemeRepository = customThemeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional(readOnly = true)
    public List<ThemeDto> getThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeDto::from)
                .toList();
    }

    public ThemeDto saveTheme(final SaveThemeRequest saveThemeRequest) {
        final Theme savedTheme = themeRepository.save(saveThemeRequest.toTheme());
        return ThemeDto.from(savedTheme);
    }

    public void deleteTheme(final Long themeId) {
        validateReservationOfIncludeThemeExist(themeId);
        themeRepository.deleteById(themeId);
    }

    private void validateReservationOfIncludeThemeExist(final Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new IllegalArgumentException("예약에 포함된 테마 정보는 삭제할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public ReservationTimeAvailabilities getAvailableReservationTimes(final LocalDate date, final Long themeId) {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final List<Reservation> reservations = reservationRepository.findAllByDateAndTheme_Id(new ReservationDate(date), themeId);

        return ReservationTimeAvailabilities.of(reservationTimes, reservations);
    }

    @Transactional(readOnly = true)
    public List<ThemeDto> getPopularThemes() {
        final ReservationDate startAt = new ReservationDate(LocalDate.now().minusDays(7));
        final ReservationDate endAt = new ReservationDate(LocalDate.now().minusDays(1));
        final int maximumThemeCount = 10;

        return customThemeRepository.findPopularThemes(startAt, endAt, maximumThemeCount)
                .stream()
                .map(ThemeDto::from)
                .toList();
    }
}
