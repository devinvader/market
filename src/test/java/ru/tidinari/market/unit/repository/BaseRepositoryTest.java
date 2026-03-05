package ru.tidinari.market.unit.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.tidinari.market.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public abstract class BaseRepositoryTest {
}