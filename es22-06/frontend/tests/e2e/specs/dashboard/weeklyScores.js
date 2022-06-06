describe('Student Walkthrough', () => {
    beforeEach(() => {
      //create quiz
      cy.demoTeacherLogin();
      cy.createQuestion(
        'Question Title',
        'Question',
        'Option',
        'Option',
        'Option',
        'Correct'
      );
      cy.createQuestion(
        'Question Title2',
        'Question',
        'Option',
        'Option',
        'Option',
        'Correct'
      );
      cy.createQuizzWith2Questions(
        'Quiz Title',
        'Question Title',
        'Question Title2'
      );
      cy.contains('Logout').click();
    });

    afterEach(() => {
        cy.deleteWeeklyScores();
        cy.deleteQuestionsAndAnswers();
    
    });

  
    it('student creates discussion', () => {
        cy.intercept('GET', '**/students/dashboards/executions/*').as('getDashboard');
        cy.intercept('GET', '**/students/dashboard/weeklyscore/get/*').as('getWeeklyScores');
        cy.intercept('PUT', '**/students/dashboard/weeklyscore/update/*').as('UpdateWeekScores');

        cy.demoStudentLogin();

        cy.solveQuizz('Quiz Title', 2);

        cy.get('[data-cy="dashboardMenuButton"]').click();
        cy.wait('@getDashboard');

        cy.get('[data-cy="weeklyScoresMenuButton"]').click();
        cy.wait('@getWeeklyScores');
        

        cy.get('[data-cy="updateWeekScoresButton"]').click();
        cy.wait('@getWeeklyScores');

        
        cy.get('[data-cy="deleteWeeklyScoresButton"]').should('have.length',1).click();
        cy.closeErrorMessage();

        cy.createWeeklyScore()

        cy.get('[data-cy="updateWeekScoresButton"]').click();
        cy.wait('@getWeeklyScores');

        cy.intercept('DELETE', '**/students/weeklyScore/detele/*').as('deleteWeeklyScores');
        
        cy.get(':nth-child(2) > :nth-child(1) > [data-cy="deleteWeeklyScoresButton"]').should('have.length',1).click();
        cy.wait('@deleteWeeklyScores');

    
                
        //   cy.createDiscussion('DISCUSSAO);
        cy.contains('Logout').click();
        Cypress.on('uncaught:exception', (err, runnable) => {
            // returning false here prevents Cypress from
            // failing the test
            return false;
        });
    });
});