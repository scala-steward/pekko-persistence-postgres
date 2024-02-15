/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.query.javadsl

import org.apache.pekko.NotUsed
import org.apache.pekko.persistence.postgres.query.scaladsl.{PostgresReadJournal => ScalaPostgresReadJournal}
import org.apache.pekko.persistence.query.{EventEnvelope, Offset}
import org.apache.pekko.persistence.query.javadsl._
import org.apache.pekko.stream.javadsl.Source

object PostgresReadJournal {
  final val Identifier = ScalaPostgresReadJournal.Identifier
}

class PostgresReadJournal(journal: ScalaPostgresReadJournal)
    extends ReadJournal
    with CurrentPersistenceIdsQuery
    with PersistenceIdsQuery
    with CurrentEventsByPersistenceIdQuery
    with EventsByPersistenceIdQuery
    with CurrentEventsByTagQuery
    with EventsByTagQuery {

  /** Same type of query as `persistenceIds` but the event stream is completed immediately when it reaches the end of
    * the "result set". Events that are stored after the query is completed are not included in the event stream.
    */
  override def currentPersistenceIds(): Source[String, NotUsed] =
    journal.currentPersistenceIds().asJava

  /** `persistenceIds` is used to retrieve a stream of all `persistenceId`s as strings.
    *
    * The stream guarantees that a `persistenceId` is only emitted once and there are no duplicates. Order is not
    * defined. Multiple executions of the same stream (even bounded) may emit different sequence of `persistenceId`s.
    *
    * The stream is not completed when it reaches the end of the currently known `persistenceId`s, but it continues to
    * push new `persistenceId`s when new events are persisted. Corresponding query that is completed when it reaches the
    * end of the currently known `persistenceId`s is provided by `currentPersistenceIds`.
    */
  override def persistenceIds(): Source[String, NotUsed] =
    journal.persistenceIds().asJava

  /** Same type of query as `eventsByPersistenceId` but the event stream is completed immediately when it reaches the
    * end of the "result set". Events that are stored after the query is completed are not included in the event stream.
    */
  override def currentEventsByPersistenceId(
      persistenceId: String,
      fromSequenceNr: Long,
      toSequenceNr: Long
  ): Source[EventEnvelope, NotUsed] =
    journal.currentEventsByPersistenceId(persistenceId, fromSequenceNr, toSequenceNr).asJava

  /** `eventsByPersistenceId` is used to retrieve a stream of events for a particular persistenceId.
    *
    * The `EventEnvelope` contains the event and provides `persistenceId` and `sequenceNr` for each event. The
    * `sequenceNr` is the sequence number for the persistent actor with the `persistenceId` that persisted the event.
    * The `persistenceId` + `sequenceNr` is an unique identifier for the event.
    *
    * `fromSequenceNr` and `toSequenceNr` can be specified to limit the set of returned events. The `fromSequenceNr` and
    * `toSequenceNr` are inclusive.
    *
    * The `EventEnvelope` also provides the `offset` that corresponds to the `ordering` column in the Journal table. The
    * `ordering` is a sequential id number that uniquely identifies the position of each event, also across different
    * `persistenceId`. The `Offset` type is `org.apache.pekko.persistence.query.Sequence` with the `ordering` as the
    * offset value. This is the same `ordering` number as is used in the offset of the `eventsByTag` query.
    *
    * The returned event stream is ordered by `sequenceNr`.
    *
    * Causality is guaranteed (`sequenceNr`s of events for a particular `persistenceId` are always ordered in a sequence
    * monotonically increasing by one). Multiple executions of the same bounded stream are guaranteed to emit exactly
    * the same stream of events.
    *
    * The stream is not completed when it reaches the end of the currently stored events, but it continues to push new
    * events when new events are persisted. Corresponding query that is completed when it reaches the end of the
    * currently stored events is provided by `currentEventsByPersistenceId`.
    */
  override def eventsByPersistenceId(
      persistenceId: String,
      fromSequenceNr: Long,
      toSequenceNr: Long
  ): Source[EventEnvelope, NotUsed] =
    journal.eventsByPersistenceId(persistenceId, fromSequenceNr, toSequenceNr).asJava

  /** Same type of query as `eventsByTag` but the event stream is completed immediately when it reaches the end of the
    * "result set". Events that are stored after the query is completed are not included in the event stream.
    */
  override def currentEventsByTag(tag: String, offset: Offset): Source[EventEnvelope, NotUsed] =
    journal.currentEventsByTag(tag, offset).asJava

  /** Query events that have a specific tag.
    *
    * The consumer can keep track of its current position in the event stream by storing the `offset` and restart the
    * query from a given `offset` after a crash/restart. The offset is exclusive, i.e. the event corresponding to the
    * given `offset` parameter is not included in the stream.
    *
    * For pekko-persistence-postgres the `offset` corresponds to the `ordering` column in the Journal table. The
    * `ordering` is a sequential id number that uniquely identifies the position of each event within the event stream.
    * The `Offset` type is `org.apache.pekko.persistence.query.Sequence` with the `ordering` as the offset value.
    *
    * The returned event stream is ordered by `offset`.
    *
    * The stream is not completed when it reaches the end of the currently stored events, but it continues to push new
    * events when new events are persisted. Corresponding query that is completed when it reaches the end of the
    * currently stored events is provided by [[CurrentEventsByTagQuery#currentEventsByTag]].
    */
  override def eventsByTag(tag: String, offset: Offset): Source[EventEnvelope, NotUsed] =
    journal.eventsByTag(tag, offset).asJava
}
