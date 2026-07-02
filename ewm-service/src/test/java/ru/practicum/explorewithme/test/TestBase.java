package ru.practicum.explorewithme.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.dto.NewCategoryDto;
import ru.practicum.explorewithme.dto.UpdateCategoryDto;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@ExtendWith(MockitoExtension.class)
public class TestBase {
    protected static final LocalDateTime NOW = LocalDateTime.now();

    protected <M> void assertMethodCall(M mock, Consumer<M> methodCall) {
        methodCall.accept(Mockito.verify(
                mock,
                Mockito.times(1))
        );
    }

    protected NewCategoryDto buildNewCategoryDto() {
        return NewCategoryDto.builder()
                .name("Category Name")
                .build();
    }

    protected UpdateCategoryDto buildUpdateCategoryDto() {
        return UpdateCategoryDto.builder()
                .name("Category Name Update")
                .build();
    }
}
