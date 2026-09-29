package com.w385.demo.movements;

import com.w385.template.database.DatabaseTestSuite;
import com.w385.template.database.SqlResourceReader;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class MovementRepositoryTests extends DatabaseTestSuite
{
	private final MovementRepository repository = new MovementRepository(this.database);

	private static final Instant NOW = Instant.now();

	@Before
	public void createTable() throws IOException
	{
		String tableCreationQuery = new SqlResourceReader()
			.read("/demo/sql/create-database.sql")
			.get(0);
		this.database.execute(tableCreationQuery);
	}

	@Test
	public void emitterMapping()
	{
		// given: a movement stored by any client
		String emitter = "any client";
		var movement = new Movement(emitter, NOW, 0, 0, 0);

		// when: reading it back
		this.repository.insert(movement);
		Movement persisted = this.repository.fetch().get(0);

		// then: the emitter should be as set
		assertThat(persisted.emitter(), is(emitter));
	}

	@Test
	public void timestampMapping()
	{
		// given: a movement stored with a given timestamp
		Instant timestamp = Instant.now();
		var movement = new Movement("", timestamp, 0, 0, 0);

		// when: reading it back
		this.repository.insert(movement);
		Movement persisted = this.repository.fetch().get(0);

		// then: the timestamp should be as set
		assertThat(persisted.timestamp(), is(timestamp));
	}

	@Test
	public void abscissaMapping()
	{
		// given: a movement stored with a given abscissa
		int abscissa = 42;
		var movement = new Movement("", NOW, abscissa, 0, 0);

		// when: reading it back
		this.repository.insert(movement);
		Movement persisted = this.repository.fetch().get(0);

		// then: the abscissa should be as set
		assertThat(persisted.abscissa(), is(abscissa));
	}

	@Test
	public void ordinateMapping()
	{
		// given: a movement stored with a given ordinate
		int ordinate = 42;
		var movement = new Movement("", NOW, 0, ordinate, 0);

		// when: reading it back
		this.repository.insert(movement);
		Movement persisted = this.repository.fetch().get(0);

		// then: the ordinate should be as set
		assertThat(persisted.ordinate(), is(ordinate));
	}

	@Test
	public void floorMapping()
	{
		// given: a movement stored with a given floor
		int floor = 42;
		var movement = new Movement("", NOW, 0, 0, floor);

		// when: reading it back
		this.repository.insert(movement);
		Movement persisted = this.repository.fetch().get(0);

		// then: the floor should be as set
		assertThat(persisted.floor(), is(floor));
	}

	@Test
	public void idIsSetAfterInsertion()
	{
		// given: a movement that has not been persisted yet
		var movement = new Movement("any", NOW, 0, 0, 0);

		// when: inserting it into the database
		this.repository.insert(movement);

		// then: its ID should now be assigned
		assertThat(movement.getId(), is(1));
	}

	@Test
	public void idIsFetched()
	{
		// given: a movement stored in database
		var movement = new Movement("any", NOW, 0, 0, 0);
		this.repository.insert(movement);

		// when: reading it from the database
		var persisted = this.repository.fetch().get(0);

		// then: its ID should now be assigned
		assertThat(persisted.getId(), is(1));
	}

	@Test
	public void idIsIgnoredForInsertion()
	{
		// given: a movement stored in database
		var movement = new Movement("any", NOW, 0, 0, 0);
		this.repository.insert(movement);

		// when: persisting it again
		this.repository.insert(movement);

		// then: newly persisted movement should have its own ID
		List<Movement> moves = this.repository.fetch();
		assertThat(moves.get(0).getId(), is(not(moves.get(1).getId())));
	}
}
