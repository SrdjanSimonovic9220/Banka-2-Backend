package rs.edu.raf.BankService.service;

import org.springframework.stereotype.Service;
import rs.edu.raf.BankService.data.dto.*;
import rs.edu.raf.BankService.data.entities.accounts.Account;
import rs.edu.raf.BankService.exception.*;

import java.util.List;

@Service
public interface AccountService {

    boolean userAccountUserProfileConnectionAttempt(AccountNumberDto accountNumberDto)
            throws UserAccountAlreadyAssociatedWithUserProfileException, UserAccountInProcessOfBindingWithUserProfileException, AccountNotFoundException;

    boolean confirmActivationCode(String accountNumber, Integer code)
            throws ActivationCodeExpiredException, ActivationCodeDoesNotMatchException;

    DomesticCurrencyAccountDto createDomesticCurrencyAccount(DomesticCurrencyAccountDto dto)
            throws AccountNumberAlreadyExistException;

    ForeignCurrencyAccountDto createForeignCurrencyAccount(ForeignCurrencyAccountDto dto)
            throws AccountNumberAlreadyExistException;

    BusinessAccountDto createBusinessAccount(BusinessAccountDto dto)
            throws AccountNumberAlreadyExistException;

    SavedAccountDto createSavedAccount(Long accountId, SavedAccountDto dto);

    List<Account> findAccountsByEmail(String email);
    SavedAccountDto updateSavedAccount(Long accountId, String savedAccountNumber, SavedAccountDto dto);

    void deleteSavedAccount(Long accountId, String savedAccountNumber);
}
