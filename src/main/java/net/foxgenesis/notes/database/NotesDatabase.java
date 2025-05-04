package net.foxgenesis.notes.database;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.dv8tion.jda.api.entities.Member;

@Repository
public interface NotesDatabase extends JpaRepository<Note, Integer>{
	 Page<Note> findAllByGuildAndMember(long guild, long member, Pageable pageable);
	 
	 default Page<Note> findAllByMember(Member member, Pageable pagable) {
		 return findAllByGuildAndMember(member.getGuild().getIdLong(), member.getIdLong(), pagable);
	 }
}
