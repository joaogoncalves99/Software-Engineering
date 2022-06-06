<template>
  <div class="container">
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :items="weekScores"
        :sort-by="['creationDate']"
        sort-desc
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
      >
        <template v-slot:top>
          <v-card-title>
            <span class="text-subtitle-1">WeeklyScore</span>

            <v-spacer />
            <v-btn
              color="primary"
              dark
              data-cy="updateWeekScoresButton"
              @click="updateWeeklyScore"
              >Refresh</v-btn
            >
          </v-card-title>
        </template>

        <template v-slot:[`item.action`]="{ item }">  
          <v-tooltip bottom >
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                v-on="on"
                data-cy="deleteWeeklyScoresButton"
                @click="deleteWeeklyScore(item)"
                color="red"
                >delete</v-icon
              >
            </template>
            <span>Delete Question</span>
          </v-tooltip>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import WeeklyScores from "@/models/dashboard/WeeklyScores";
import RemoteServices from '@/services/RemoteServices';
import AnimatedNumber from '@/components/AnimatedNumber.vue';

@Component({
  components: { AnimatedNumber },
})
export default class WeeklyScoreView extends Vue {
  @Prop({ type: Number, required: true }) readonly dashboardId !: number;
  weekScores: WeeklyScores[] = [];
  


  async created() {
    await this.$store.dispatch('loading');
    try {
      
      this.weekScores= await RemoteServices.getWeeklyScore(this.dashboardId);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

   async updateWeeklyScore() {
    
    try {
      await RemoteServices.updateWeeklyScore(this.dashboardId);
      this.weekScores= await RemoteServices.getWeeklyScore(this.dashboardId);
      let dashboard = await RemoteServices.getUserDashboard();
      this.$emit('refresh', dashboard.lastCheckWeeklyScores)
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }


  async deleteWeeklyScore(weeklyScore: WeeklyScores) {
    try {
      await RemoteServices.deleteWeeklyScore(weeklyScore.id);
      this.weekScores = this.weekScores.filter(
        (weeklyscore1) => weeklyscore1.id != weeklyScore.id
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }


  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '5px',
      sortable: false,
    },
    { text: 'Week', value: 'week', width: '50%', align: 'left' },
    {
      text: 'Number Answered',
      value: 'numberAnswered',
      width: '5px',
      align: 'center',
    },
    {
      text: 'Uniquely Answered',
      value: 'uniquelyAnswered',
      width: '5px',
      align: 'center',
    },
    {
      text: 'Percentage Answered',
      value: 'percentageCorrect',
      width: '150px',
      align: 'center',
    },
  ];
}

</script>

<style lang="scss" scoped>
</style>

function dashboardId(dashboardId: any): any {
  throw new Error('Function not implemented.');
}

function dashboardId(dashboardId: any): any {
  throw new Error('Function not implemented.');
}

function dashboardId(dashboardId: any): any {
  throw new Error('Function not implemented.');
}
