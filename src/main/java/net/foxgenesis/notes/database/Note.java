package net.foxgenesis.notes.database;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.foxgenesis.springJDA.annotation.Snowflake;

@Entity
@Table(indexes = { @Index(name = "guild_member", columnList = "guild, member") })
public class Note {
	private static final String NOTE_PATTERN = "**[%s]** %s - **<@%d>**\n%s";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Snowflake
	@Column(updatable = false, nullable = false)
	private long guild;

	@Snowflake
	@Column(updatable = false, nullable = false)
	private long member;

	@Snowflake
	@Column(updatable = false, nullable = false)
	private long moderator;

	@CurrentTimestamp(source = SourceType.VM)
	@Column(updatable = false, nullable = false)
	private OffsetDateTime time;

	@Length(min = 3, max = 200)
	@Column(length = 200, nullable = false)
	private String note;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getGuild() {
		return guild;
	}

	public void setGuild(long guild) {
		this.guild = guild;
	}

	public long getMember() {
		return member;
	}

	public void setMember(long member) {
		this.member = member;
	}

	public long getModerator() {
		return moderator;
	}

	public void setModerator(long moderator) {
		this.moderator = moderator;
	}

	public OffsetDateTime getTime() {
		return time;
	}

	public void setTime(OffsetDateTime time) {
		this.time = time;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public int hashCode() {
		return Objects.hash(guild, id, member, moderator, note, time);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Note other = (Note) obj;
		return guild == other.guild && id == other.id && member == other.member && moderator == other.moderator
				&& Objects.equals(note, other.note) && time == other.time;
	}

	@Override
	public String toString() {
		return "Note [id=" + id + ", guild=" + guild + ", member=" + member + ", moderator=" + moderator + ", time="
				+ time + ", note=" + note + "]";
	}

	public String formatted() {
		return NOTE_PATTERN.formatted(getId(), TimeFormat.RELATIVE.atInstant(getTime().toInstant()), getModerator(),
				MarkdownSanitizer.sanitize(getNote()));
	}
}
