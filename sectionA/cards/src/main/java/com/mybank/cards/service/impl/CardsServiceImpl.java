package com.mybank.cards.service.impl;

import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.mybank.cards.constants.CardsConstants;
import com.mybank.cards.dto.CardsDto;
import com.mybank.cards.entity.Cards;
import com.mybank.cards.exception.CardAlreadyExistsException;
import com.mybank.cards.exception.ResourceNotFoundException;
import com.mybank.cards.mapper.CardsMapper;
import com.mybank.cards.repository.CardsRepository;
import com.mybank.cards.service.ICardsService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CardsServiceImpl implements ICardsService {

	private CardsRepository cardsRepository;

	@Override
	public void createCard(String mobileNumber) {
		Optional<Cards> optionalCards = cardsRepository.findByMobileNumber(mobileNumber);
		if (optionalCards.isPresent()) {
			throw new CardAlreadyExistsException("Card already registered with given mobileNumber " + mobileNumber);
		}
		cardsRepository.save(createNewCard(mobileNumber));

	}

	private Cards createNewCard(String mobileNumber) {
		Cards newCard = new Cards();
		long randomCardNumber = 100000000000L + new Random().nextInt(900000000);
		newCard.setCardNumber(Long.toString(randomCardNumber));
		newCard.setMobileNumber(mobileNumber);
		newCard.setCardType(CardsConstants.CREDIT_CARD);
		newCard.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
		newCard.setAmountUsed(0);
		newCard.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);
		return newCard;
	}

	@Override
	public CardsDto fetchCard(String mobileNumber) {
		Cards cards = cardsRepository.findByMobileNumber(mobileNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber));
		return CardsMapper.mapToCardsDto(cards, new CardsDto());
	}

	@Override
	public boolean updateCard(CardsDto cardsDto) {
		Cards cards = cardsRepository.findByCardNumber(cardsDto.getCardNumber())
				.orElseThrow(() -> new ResourceNotFoundException("Card", "CardNumber", cardsDto.getCardNumber()));
		CardsMapper.mapToCards(cardsDto, cards);
		cardsRepository.save(cards);
		return true;
	}

	@Override
	public boolean deleteCard(String mobileNumber) {
		Cards cards = cardsRepository.findByMobileNumber(mobileNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber));
		cardsRepository.deleteById(cards.getCardId());
		return true;
	}

}
