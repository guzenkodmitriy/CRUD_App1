package com.guzenko.springcourse.dao;

import com.guzenko.springcourse.models.Person;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PersonDAO {

    private final JdbcTemplate jdbcTemplate;

    public PersonDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Person> index() {
        return jdbcTemplate.query("SELECT * FROM person", new BeanPropertyRowMapper<>(Person.class));
    }

    public Optional<Person> show(String email) {
        return jdbcTemplate.query("SELECT * FROM person WHERE email = ?", new Object[] {email},
                new BeanPropertyRowMapper<>(Person.class)).stream().findAny();
    }

    public Person show(int id) {
       return jdbcTemplate.queryForObject("SELECT * FROM person WHERE id = ?", new Object[]{id}, new BeanPropertyRowMapper<>(Person.class));
    }

    public void save(Person person) {
        jdbcTemplate.update("INSERT INTO person(name, age, email, address) VALUES (?, ?, ?, ?)"
                , person.getName()
                , person.getAge()
                , person.getEmail()
                , person.getAddress());
    }

    public void update(int id, Person updatedPerson) {
        jdbcTemplate.update("update Person set name = ?, age = ?, email = ?, address = ? where id = ?"
                , updatedPerson.getName()
                , updatedPerson.getAge()
                , updatedPerson.getEmail()
                , updatedPerson.getAddress()
                , id);

    }

    public void delete(int id) {
        jdbcTemplate.update("delete from person where id = ?", id);
    }

    /////////////////////////////////////////////////////
    /// Тестируем производительность пакетной вставки ///
    /////////////////////////////////////////////////////

    public void testMultipleUpdate() {
        List<Person> people = create1000People();

        long before = System.currentTimeMillis();

        for (Person person : people) {
            jdbcTemplate.update("INSERT INTO person(name, age, email) VALUES (?, ?, ?)", person.getName(), person.getAge(), person.getEmail());
        }
        long after = System.currentTimeMillis();
        System.out.println("Time taken: " + (after - before));

    }

    public void testBatchUpdate() {
        List<Person> people = create1000People();

        long before = System.currentTimeMillis();

        jdbcTemplate.batchUpdate("INSERT INTO person(name, age, email) VALUES (?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setString(1, people.get(i).getName());
                        preparedStatement.setInt(2, people.get(i).getAge());
                        preparedStatement.setString(3, people.get(i).getEmail());
                    }

                    @Override
                    public int getBatchSize() {
                        return people.size();
                    }
                });

        long after = System.currentTimeMillis();
        System.out.println("Time taken: " + (after - before));
    }

    private List<Person> create1000People() {
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            people.add(new Person(i, "Name" + i, 30, "Emailtest" + i + "@gmail.com", "some address"));
        }

        return people;
    }

}