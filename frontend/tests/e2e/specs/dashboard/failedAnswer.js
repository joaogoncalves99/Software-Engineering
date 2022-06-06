describe('Student Walkthrough', () => {
    beforeEach(() => {
      //create quiz
      cy.demoTeacherLogin();
      cy.createQuestion(
        'Question FailedAnswer 1',
        'Question',
        'Option',
        'Option',
        'ChooseThisWrong',
        'Correct'
      );
      cy.createQuestion(
        'Question FailedAnswer 2',
        'Question',
        'Option',
        'Option',
        'ChooseThisWrong',
        'Correct'
      );
      cy.createQuizzWith2Questions(
        'Failed Answers Title',
        'Question FailedAnswer 1',
        'Question FailedAnswer 2'
      );
      cy.contains('Logout').click();
    });

    afterEach(() => {
        cy.deleteFailedAnswers();
        cy.deleteQuestionsAndAnswers();
    
    });

  
    it('student creates discussion', () => {
        cy.intercept('GET', '**/students/dashboards/executions/*').as('getDashboard');
        cy.intercept('GET', '**/students/failedAnswers/dashboards/*').as('getFailedAnswer');
        cy.intercept('PUT', '**/students/dashboards/updates/*').as('updateFailedAnswer');

        cy.demoStudentLogin();

        cy.solveQuizzFailedAnswer('Failed Answers Title',2);
   

        cy.get('[data-cy="dashboardMenuButton"]').click();
        cy.wait('@getDashboard');

        cy.get('[data-cy="failedAnswersMenuButton"]').click();
        cy.wait('@getFailedAnswer');
        

        cy.get('[data-cy="updateFailedAnswerButton"]').click();
        cy.wait('@getFailedAnswer');

        cy.get(':nth-child(1) > .text-left > [data-cy="showStudentViewDialogButton"]').click();
        cy.get('.v-card__actions > .v-btn > .v-btn__content').click();

        cy.get(':nth-child(1) > .text-left > [data-cy="deleteFailedAnswerButton"]').should('have.length', 1).click();
        cy.closeErrorMessage();

        cy.setFailedAnswersAsOld()

        cy.get('[data-cy="updateFailedAnswerButton"]').click();
        cy.wait('@getFailedAnswer');

        cy.intercept('DELETE', '**/students/dashboards/failedAnswers/*').as('deleteFailedAnswer');
        
        cy.get(':nth-child(2) > :nth-child(1) > [data-cy="deleteFailedAnswerButton"]').should('have.length', 1).click();
        cy.wait('@deleteFailedAnswer');

    
                
        //   cy.createDiscussion('DISCUSSAO);
        cy.contains('Logout').click();
        Cypress.on('uncaught:exception', (err, runnable) => {
            // returning false here prevents Cypress from
            // failing the test
            return false;
        });
    });
});