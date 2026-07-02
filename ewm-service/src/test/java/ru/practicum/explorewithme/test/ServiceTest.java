package ru.practicum.explorewithme.test;

import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.dto.NewCategoryDto;
import ru.practicum.explorewithme.dto.UpdateCategoryDto;
import ru.practicum.explorewithme.entity.Category;

import java.util.Optional;

public class ServiceTest extends TestBase {
    protected <O> void assertEquals(O actual, O expected) {
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    protected <T extends Throwable> void assertException(Throwable thrown, Class<T> expectedThrowable) {
        Assertions.assertThat(thrown)
                .isInstanceOf(expectedThrowable);
    }

    protected <E> void whenSaveReturns(JpaRepository<E, Long> repository, E saved) {
        Mockito.when(repository.save(Mockito.any()))
                .thenAnswer(invocationOnMock -> saved);
    }

    protected <E> void whenSaveThrows(JpaRepository<E, Long> repository, Throwable throwable) {
        Mockito.when(repository.save(Mockito.any()))
                .thenThrow(throwable);
    }

    protected <E> void whenEntityExistIn(JpaRepository<E, Long> repository) {
        Mockito.when(repository.existsById(Mockito.anyLong()))
                .thenReturn(true);
    }

    protected <E> void whenEntityAbsentIn(JpaRepository<E, Long> repository) {
        Mockito.when(repository.existsById(Mockito.anyLong()))
                .thenReturn(false);
    }

    protected <E> void whenEntityFoundIn(JpaRepository<E, Long> repository, E entity) {
        Mockito.when(repository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(entity));
    }

    protected <E> void whenEntityNotFoundIn(JpaRepository<E, Long> repository) {
        Mockito.when(repository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
    }

    protected <E> void doNothingOnDeleteIn(JpaRepository<E, Long> repository) {
        Mockito.doNothing()
                .when(repository).deleteById(Mockito.anyLong());
    }
}
